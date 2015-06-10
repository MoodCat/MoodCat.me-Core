package me.moodcat.backend.rooms;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.persist.Transactional;

/**
 * The instance object of the rooms.
 */
@Slf4j
public class RoomInstance {

    /**
     * Number of chat messages to cache for each room.
     */
    public static final int MAXIMAL_NUMBER_OF_CHAT_MESSAGES = 100;

    /**
     * {@link SongInstanceFactory} to create {@link SongInstance SongInstances} with.
     */
    private final SongInstanceFactory songInstanceFactory;

    /**
     * {@link Provider} for a {@link RoomDAO}.
     */
    private final Provider<RoomDAO> roomDAOProvider;

    /**
     * {@link UnitOfWorkSchedulingService} to schedule tasks in a unit of work.
     */
    private final UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    /**
     * The room index.
     *
     * @return the room id
     */
    @Getter
    private final int id;

    /**
     * The room name.
     *
     * @return the room name
     */
    @Getter
    private final String name;

    /**
     * Keep track of the message index.
     */
    private final ChatMessageIdGenerator chatMessageIdGenerator;

    /**
     * Factory for chat messages.
     */
    private final ChatMessageFactory chatMessageFactory;

    /**
     * The cached messages in order to speed up retrieval.
     */
    private final Deque<ChatMessageInstance> messages;

    /**
     * The current song.
     */
    private final AtomicReference<SongInstance> currentSong;

    /**
     * Has changed flag.
     */
    private final AtomicBoolean hasChanged;

    /**
     * ChatRoomInstance's constructur, will create a roomInstance from a room
     * and start the timer for the current song.
     *
     * @param songInstanceFactory
     *            SongInstanceFactory to create Songs.
     * @param roomDAOProvider
     *            Provider to get RoomDAOs when in a Unit of Work.
     * @param unitOfWorkSchedulingService
     *            Scheduling service to run tasks in a Unit of Work.
     * @param room
     *            the room used to create the roomInstance.
     */
    @AssistedInject
    public RoomInstance(final SongInstanceFactory songInstanceFactory,
            final Provider<RoomDAO> roomDAOProvider,
            final UnitOfWorkSchedulingService unitOfWorkSchedulingService,
            final ChatMessageFactory chatMessageFactory,
            @Assisted final Room room) {

        Preconditions.checkNotNull(room);

        this.songInstanceFactory = songInstanceFactory;
        this.roomDAOProvider = roomDAOProvider;
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;
        this.chatMessageFactory = chatMessageFactory;

        this.id = room.getId();
        this.name = room.getName();

        final Collection<ChatMessage> messages = room.getChatMessages();
        this.chatMessageIdGenerator = new ChatMessageIdGenerator(room);
        this.messages = getChatMessageModels(messages);

        this.currentSong = new AtomicReference<>();
        this.hasChanged = new AtomicBoolean(false);

        this.scheduleSyncTimer();
        this.playNext(room.getCurrentSong());
        log.info("Initialized room instance {}", this);
    }

    private static LinkedList<ChatMessageInstance> getChatMessageModels(
            final Collection<ChatMessage> messages) {
        return Lists.newLinkedList(messages.stream()
                .map(ChatMessageInstance::create)
                .collect(Collectors.toList()));
    }

    /**
     * Play a next song. Will fetch from the history if no songs can be found.
     */
    @Transactional
    public void playNext() {
        final RoomDAO roomDAO = this.roomDAOProvider.get();
        final Room room = roomDAO.findById(id);
        final List<Song> history = room.getPlayHistory();
        final Song previousSong = room.getCurrentSong();

        if (previousSong == null) {
            throw new IllegalStateException("Room should be playing a song");
        }
        history.add(previousSong);

        final List<Song> playQueue = room.getPlayQueue();
        if (playQueue.isEmpty()) {
            playQueue.addAll(history);
        }

        playNext(playQueue.remove(0));
        hasChanged.set(true);
        this.merge();
    }

    @Transactional
    protected void playNext(final Song song) {
        assert song != null;
        final SongInstance songInstance = songInstanceFactory.create(song);
        this.currentSong.set(songInstance);
        log.info("Room {} now playing {}", this.id, song);

        final ScheduledFuture<?> future = this.unitOfWorkSchedulingService.scheduleAtFixedRate(
                songInstance::incrementTime, 1L, 1L, TimeUnit.SECONDS);

        // Observer: Stop the increment time task when the song is finished
        songInstance.addObserver((observer, arg) -> future.cancel(false));
        // Observer: Play the next song when the song is finished
        songInstance.addObserver((observer, arg) -> playNext());

        hasChanged.set(true);
    }

    /**
     * Add all the songs to the queue.
     *
     * @param songs
     *            The songs to add to the queue.
     */
    @Transactional
    public void queue(final Collection<Song> songs) {
        Preconditions.checkNotNull(songs);

        final RoomDAO roomDAO = this.roomDAOProvider.get();
        final Room room = roomDAO.findById(id);
        room.getPlayQueue().addAll(songs);
        hasChanged.set(true);
    }

    /**
     * Sync room messages to the database.
     */
    private void scheduleSyncTimer() {
        this.unitOfWorkSchedulingService.scheduleAtFixedRate(this::merge,
                1, 1, TimeUnit.MINUTES);
    }

    /**
     * Store a message in the instance.
     *
     * @param model
     *            the message to send.
     */
    public ChatMessageModel sendMessage(final ChatMessageModel model) {
        Preconditions.checkNotNull(model);

        model.setId(chatMessageIdGenerator.generateId());
        model.setTimestamp(System.currentTimeMillis());

        ChatMessageInstance chatMessage = new ChatMessageInstance(1, model);
        messages.addLast(chatMessage);

        if (messages.size() > MAXIMAL_NUMBER_OF_CHAT_MESSAGES) {
            messages.removeFirst();
        }

        hasChanged.set(true);
        log.info("Sending message {} in room {}", chatMessage, this);
        return model;
    }

    /**
     * Merge the changes of the instance in the database.
     */
    protected void merge() {
        if (hasChanged.getAndSet(false)) {
            log.info("Merging changes in room {}", this);
            this.unitOfWorkSchedulingService.performInUnitOfWork(() -> {
                final RoomDAO roomDAO = this.roomDAOProvider.get();
                final Room room = roomDAO.findById(id);
                room.getChatMessages().addAll(messages.stream()
                        .map(message -> chatMessageFactory.create(room, message))
                        .collect(Collectors.toList()));
                room.setCurrentSong(getCurrentSong());

                try {
                    return roomDAO.merge(room);
                }
                    catch (Exception e) {
                        log.error("Failed to persist room {}", room);
                        return null;
                    }
                });
        }
    }

    /**
     * The cached messages in order to speed up retrieval.
     *
     * @return The latest {@link #MAXIMAL_NUMBER_OF_CHAT_MESSAGES} messages.
     */
    public List<ChatMessageModel> getMessages() {
        return this.messages.stream()
                .map(ChatMessageInstance::transform)
                .collect(Collectors.toList());
    }

    /**
     * Get the instance's current song.
     *
     * @return the current song.
     */
    @Transactional
    public Song getCurrentSong() {
        return this.currentSong.get().getSong();
    }

    /**
     * Get the progress of the current song.
     *
     * @return the progress in seconds.
     */
    public long getCurrentTime() {
        return this.currentSong.get().getTime();
    }

}
