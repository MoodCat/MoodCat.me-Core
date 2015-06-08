package me.moodcat.backend;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

/**
 * The backend of rooms, initializes room instances and keeps track of time and messages.
 */
@Slf4j
@Singleton
public class RoomBackend extends AbstractLifeCycleListener {

    /**
     * The size of executorService's thread pool.
     */
    private static final int THREAD_POOL_SIZE = 4;

    /**
     * The service to increment the room's song time.
     */
    private final ScheduledExecutorService executorService;

    /**
     * A map of room instances.
     */
    private final Map<Integer, RoomInstance> roomInstances;

    /**
     * The room DAO provider.
     */
    private final Provider<RoomDAO> roomDAOProvider;

    /**
     * The songDAO provider.
     */
    private final Provider<SongDAO> songDAOProvider;

    /**
     * The CallableInUnitOfWorkFactory which is used to perform large tasks in the background.
     */
    private final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory;

    /**
     * The constructor of the chat's backend, initializes fields and rooms.
     *
     * @param roomDAOProvider
     *            The provider for the RoomDAO.
     * @param callableInUnitOfWorkFactory
     *            The factory that can create UnitOfWorks.
     * @param chatDAOProvider
     *            The provider for the ChatDAO.
     */
    @Inject
    public RoomBackend(final Provider<RoomDAO> roomDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            final Provider<ChatDAO> chatDAOProvider,
            final LifeCycle lifeCycle) {
        this(roomDAOProvider, songDAOProvider, callableInUnitOfWorkFactory, Executors
                .newScheduledThreadPool(THREAD_POOL_SIZE));
        // Add this as lifecycle listener
        lifeCycle.addLifeCycleListener(this);
    }

    /**
     * The constructor of the chat's backend, initializes fields and rooms.
     *
     * @param roomDAOProvider
     *            The provider for the RoomDAO.
     * @param callableInUnitOfWorkFactory
     *            The factory that can create UnitOfWorks.
     * @param executorService
     *            The executor service to run multi-threaded.
     */
    protected RoomBackend(final Provider<RoomDAO> roomDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            final ScheduledExecutorService executorService) {
        this.executorService = executorService;
        this.songDAOProvider = songDAOProvider;
        this.roomDAOProvider = roomDAOProvider;
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
        this.roomInstances = Maps.newTreeMap();
    }

    /**
     * Get a room instance by its id.
     *
     * @param id
     *            the room's id
     * @return the room
     */
    public RoomInstance getRoomInstance(final int id) {
        return roomInstances.get(id);
    }

    /**
     * Initialize the rooms from the db.
     */
    public void initializeRooms() {
        performInUnitOfWork(() -> {
            final RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                    .map(RoomInstance::new)
                    .forEach(roomInstance -> roomInstances.put(roomInstance.getId(), roomInstance));
            return roomInstances;
        });
    }

    @Override
    public void lifeCycleStarted(final LifeCycle event) {
        super.lifeCycleStarted(event);
        log.info("[Lifecycle started] Creating initial rooms for {}", this);
        initializeRooms();
    }

    @Override
    public void lifeCycleStopping(final LifeCycle event) {
        log.info("[Lifecycle stopping] Shutting down executor for {}", this);
        executorService.shutdown();
    }

    @SneakyThrows
    protected <V> V performInUnitOfWork(final Callable<V> callable) {
        assert callable != null;
        final Callable<V> inUnitOfWork = callableInUnitOfWorkFactory.create(callable);
        return executorService.submit(inUnitOfWork).get();
    }

    /**
     * The instance object of the rooms.
     */
    public class RoomInstance {

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
        private final LinkedList<ChatMessage> messages;

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
         */
        public RoomInstance(final Room room) {
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
            final RoomDAO roomDAO = roomDAOProvider.get();
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
            final SongInstance songInstance = new SongInstance(song);
            currentSong.set(songInstance);
            log.info("Room {} now playing {}", this.id, song);
            final ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
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

            final RoomDAO roomDAO = roomDAOProvider.get();
            final Room room = roomDAO.findById(id);
            room.getPlayQueue().addAll(songs);
            hasChanged.set(true);
        }

        /**
         * Sync room messages to the database.
         */
        private void scheduleSyncTimer() {
            RoomBackend.this.executorService.scheduleAtFixedRate(this::merge,
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
                performInUnitOfWork(() -> {
                    final RoomDAO roomDAO = roomDAOProvider.get();
                    final Room room = roomDAO.findById(id);
                    room.setChatMessages(messages.stream().map(message -> {
                        final ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setTimestamp(message.getTimestamp());
                        chatMessage.setAuthor(message.getAuthor());
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
        public List<ChatMessage> getMessages() {
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

    /**
     * A song that is currently playing in a room.
     */
    public class SongInstance extends Observable {

        /**
         * The current time of the room's song.
         */
        private final AtomicLong currentTime;

        /**
         * The duration of the song.
         */
        private final int duration;

        /**
         * Last update.
         */
        private final AtomicLong lastUpdate;

        /**
         * Song id for the song.
         */
        private final int songId;

        /**
         * Create a new song instance.
         *
         * @param song
         *            The song this instance presents.
         */
        public SongInstance(final Song song) {
            Preconditions.checkNotNull(song);

            this.currentTime = new AtomicLong(0L);
            this.songId = song.getId();
            this.duration = song.getDuration();
            this.lastUpdate = new AtomicLong(System.currentTimeMillis());
        }

        @Transactional
        public Song getSong() {
            return songDAOProvider.get().findById(songId);
        }

        /**
         * Method used to increment the time of the current song by one second.
         */
        protected void incrementTime() {
            if (isStopped() && !hasChanged()) {
                log.debug("Song {} has finished playing", this);
                setChanged();
                notifyObservers();
            } else {
                final long now = System.currentTimeMillis();
                final long then = lastUpdate.getAndSet(now);
                // log.debug("Incremented time for song {}", this);
                currentTime.addAndGet(now - then);
            }
        }

        /**
         * Check if the song has completed.
         *
         * @return true if the song is finished
         */
        public boolean isStopped() {
            final long time = currentTime.get();
            return time >= duration;
        }

        /**
         * Get the time.
         *
         * @return the time
         */
        public long getTime() {
            return currentTime.get();
        }

    }

}
