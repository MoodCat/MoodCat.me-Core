package me.moodcat.backend.rooms;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.Vote;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.persist.Transactional;

import me.moodcat.database.entities.User;

/**
 * The instance object of the rooms.
 */
@Slf4j
public class RoomInstance {

    private static final int MESSAGE_FLOODING_TIMEOUT = 5;

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
     * The profanity checker to filter out 'bad' chatmessages.
     */
    private final ProfanityChecker profanityChecker;

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
     * The votes of the users for the current song.
     */
    private final Map<User, Vote> votes;


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
            final ProfanityChecker profanityChecker,
            @Assisted final Room room) {

        Preconditions.checkNotNull(room);
        this.profanityChecker = profanityChecker;
        this.songInstanceFactory = songInstanceFactory;
        this.roomDAOProvider = roomDAOProvider;
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;
        this.chatMessageFactory = chatMessageFactory;
        this.votes = Maps.newConcurrentMap();

        this.id = room.getId();
        this.name = room.getName();
        this.chatMessageIdGenerator = new ChatMessageIdGenerator(room);
        this.messages = getChatMessageModels(room.getChatMessages());
        this.currentSong = new AtomicReference<SongInstance>();
        this.hasChanged = new AtomicBoolean(false);

        this.scheduleSyncTimer();
        this.playNext(room.getCurrentSong());
        log.info("Initialized room instance {}", this);
    }

    private static LinkedList<ChatMessageInstance> getChatMessageModels(
            final Collection<ChatMessage> messages) {
        return Lists.newLinkedList(messages.stream()
                .map(ChatMessageInstance::create).collect(Collectors.toList()));
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

        processVotes(previousSong);

        processNextSong(room, history);
        this.merge();
    }

    @Transactional
    protected void playNext(final Song song) {
        assert song != null;
        final SongInstance songInstance = songInstanceFactory.create(song);
        this.currentSong.set(songInstance);
        log.info("Room {} now playing {}", this.id, song);

        final ScheduledFuture<?> future = this.unitOfWorkSchedulingService
                .scheduleAtFixedRate(songInstance::incrementTime, 1L, 1L,
                        TimeUnit.SECONDS);

        // Observer: Stop the increment time task when the song is finished
        songInstance.addObserver((observer, arg) -> future.cancel(false));
        // Observer: Play the next song when the song is finished
        songInstance.addObserver((observer, arg) -> playNext());

        hasChanged.set(true);
    }

    private void processVotes(final Song previousSong) {
        int nettoVotes = this.votes.values().stream()
                .mapToInt(Vote::getValue)
                .sum();

        if (nettoVotes < 0) {
            final RoomDAO roomDAO = this.roomDAOProvider.get();
            final Room room = roomDAO.findById(id);

            previousSong.addExclusionRoom(room);
        }

        this.votes.clear();
    }

    private void processNextSong(final Room room, final List<Song> history) {
        final List<Song> playQueue = room.getPlayQueue();
        if (playQueue.isEmpty()) {
            playQueue.addAll(history);
            history.clear();
        }

        playNext(playQueue.remove(0));
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
        this.unitOfWorkSchedulingService.scheduleAtFixedRate(this::merge, 1, 1,
                TimeUnit.MINUTES);
    }

    /**
     * Store a message in the instance.
     *
     * @param model
     *            the message to send.
     */
    public ChatMessageModel sendMessage(final ChatMessageModel model,
            final User user) {
        Preconditions.checkNotNull(model);
        verifyNonSpamming(user);

        updateAndSetModel(model, user);

        ChatMessageInstance chatMessage = new ChatMessageInstance(user.getId(),
                model);
        messages.addLast(chatMessage);

        if (messages.size() > MAXIMAL_NUMBER_OF_CHAT_MESSAGES) {
            messages.removeFirst();
        }

        hasChanged.set(true);
        log.info("Sending message {} in room {}", chatMessage, this);
        return model;
    }

    private void verifyNonSpamming(final User user) {
        // Our system is allowed to send messages
        if (user.getId().equals(1)) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        if (messages.stream().filter((message) -> {
            return message.getUserId() == user.getId()
                    && message.getTimestamp() + TimeUnit.SECONDS.toMillis(MESSAGE_FLOODING_TIMEOUT) > currentTime;
        }).count() > 0) {
            throw new IllegalArgumentException("You can not post within " + MESSAGE_FLOODING_TIMEOUT + " seconds");
        }
    }

    private void updateAndSetModel(final ChatMessageModel model, final User user) {
        model.setId(chatMessageIdGenerator.generateId());
        model.setTimestamp(System.currentTimeMillis());
        model.setAuthor(user.getName());
        model.setMessage(profanityChecker.clearProfanity(model.getMessage()));
    }

    /**
     * Merge the changes of the instance in the database.
     */
    protected void merge() {
        if (hasChanged.getAndSet(false)) {
            log.info("Merging changes in room {}", this.getId());
            this.unitOfWorkSchedulingService.performInUnitOfWork(this::mergeRoom);
        }
    }

    @Transactional
    private Room mergeRoom() {
        try {
            final RoomDAO roomDAO = this.roomDAOProvider.get();
            final Room room = roomDAO.findById(id);

            Collection<ChatMessage> newMessages = messages.stream()
                .map(message -> chatMessageFactory
                        .create(room, message))
                .collect(Collectors.toList());

            room.getChatMessages().addAll(newMessages);
            room.setCurrentSong(getCurrentSong());

            return roomDAO.merge(room);
        } catch (Throwable e) {
            log.error(String.format("Failed to persist room %s, due to error: %s", this.getId(), e.getMessage()), e);
            return null;
        }
    }

    /**
     * The cached messages in order to speed up retrieval.
     *
     * @return The latest {@link #MAXIMAL_NUMBER_OF_CHAT_MESSAGES} messages.
     */
    public List<ChatMessageModel> getMessages() {
        return this.messages.stream().map(ChatMessageInstance::transform)
                .collect(Collectors.toList());
    }

    /**
     * Get the instance's current song.
     *
     * @return the current song.
     */
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

    public void addVote(final User user, final Vote valueOf) {
        this.votes.put(user, valueOf);
    }

}
