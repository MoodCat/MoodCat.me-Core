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
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

/**
 * The instance object of the rooms.
 */
@Slf4j
public class RoomInstance {

    /**
	 * 
	 */
	private final RoomBackend roomBackend;

	/**
     * Number of chat messages to cache for each room.
     */
    public static final int MAXIMAL_NUMBER_OF_CHAT_MESSAGES = 100;

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
     * The cached messages in order to speed up retrieval.
     */
    private final Deque<ChatMessage> messages;

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
     * @param room
     *            the room used to create the roomInstance.
     * @param roomBackend TODO
     */
    public RoomInstance(RoomBackend roomBackend, final Room room) {
        this.roomBackend = roomBackend;
		Preconditions.checkNotNull(room);

        this.id = room.getId();
        this.name = room.getName();
        this.messages = new LinkedList<ChatMessage>(room.getChatMessages());

        this.currentSong = new AtomicReference<>();
        this.hasChanged = new AtomicBoolean(false);

        this.scheduleSyncTimer();
        this.playNext(room.getCurrentSong());
        log.info("Initialized room instance {}", this);
    }

    /**
     * Play a next song. Will fetch from the history if no songs can be found.
     */
    @Transactional
    public void playNext() {
        final RoomDAO roomDAO = this.roomBackend.roomDAOProvider.get();
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
        roomDAO.merge(room);
    }

    @Transactional
    protected void playNext(final Song song) {
        assert song != null;
        final SongInstance songInstance = new SongInstance(this.roomBackend, song);
        currentSong.set(songInstance);
        log.info("Room {} now playing {}", this.id, song);
        final ScheduledFuture<?> future = this.roomBackend.executorService.scheduleAtFixedRate(
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

        final RoomDAO roomDAO = this.roomBackend.roomDAOProvider.get();
        final Room room = roomDAO.findById(id);
        room.getPlayQueue().addAll(songs);
        hasChanged.set(true);
    }

    /**
     * Sync room messages to the database.
     */
    private void scheduleSyncTimer() {
        this.roomBackend.executorService.scheduleAtFixedRate(this::merge,
                1, 1, TimeUnit.MINUTES);
    }

    /**
     * Store a message in the instance.
     *
     * @param chatMessage
     *            the message to send.
     */
    public void sendMessage(final ChatMessage chatMessage) {
        Preconditions.checkNotNull(chatMessage);
        chatMessage.setTimestamp(System.currentTimeMillis());

        messages.addLast(chatMessage);

        if (messages.size() > MAXIMAL_NUMBER_OF_CHAT_MESSAGES) {
            messages.removeFirst();
        }

        hasChanged.set(true);
        log.info("Sending message {} in room {}", chatMessage, this);
    }

    /**
     * Merge the changes of the instance in the database.
     */
    protected void merge() {
        if (hasChanged.getAndSet(false)) {
            log.info("Merging changes in room {}", this);
            this.roomBackend.performInUnitOfWork(() -> {
                final RoomDAO roomDAO = this.roomBackend.roomDAOProvider.get();
                final Room room = roomDAO.findById(id);
                room.setChatMessages(messages.stream().map(message -> {
                    final ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setTimestamp(message.getTimestamp());
                    chatMessage.setUser(message.getUser());
                    chatMessage.setMessage(message.getMessage());
                    chatMessage.setRoom(room);
                    return chatMessage;
                }).collect(Collectors.toList()));
                room.setCurrentSong(getCurrentSong());
                return roomDAO.merge(room);
            });
        }
    }

    /**
     * The cached messages in order to speed up retrieval.
     *
     * @return The latest {@link #MAXIMAL_NUMBER_OF_CHAT_MESSAGES} messages.
     */
    public Collection<ChatMessage> getMessages() {
        return messages;
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