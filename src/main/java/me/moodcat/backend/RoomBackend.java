package me.moodcat.backend;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
     * The songDAO provider
     */
    private final Provider<SongDAO> songDAOProvider;

    /**
     * The chat DAO provider.
     */
    private Provider<ChatDAO> chatDAOProvider;

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
                .newScheduledThreadPool(THREAD_POOL_SIZE), chatDAOProvider);
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
     * @param chatDAOProvider
     *            The provider for the ChatDAO.
     */
    protected RoomBackend(final Provider<RoomDAO> roomDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            final ScheduledExecutorService executorService,
            final Provider<ChatDAO> chatDAOProvider) {
        this.executorService = executorService;
        this.songDAOProvider = songDAOProvider;
        this.roomDAOProvider = roomDAOProvider;
        this.chatDAOProvider = chatDAOProvider;
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
    public void lifeCycleStarted(LifeCycle event) {
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
         * The room index
         *
         * @return the room id
         */
        @Getter
        private final int id;

        /**
         * The room name
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
         * The current song
         */
        private final AtomicReference<SongInstance> currentSong;

        /**
         * ChatRoomInstance's constructur, will create a roomInstance from a room
         * and start the timer for the current song.
         *
         * @param room
         *            the room used to create the roomInstance.
         */
        public RoomInstance(final Room room) {
            this.name = room.getName();
            this.id = room.getId();
            this.messages = new LinkedList<ChatMessage>(room.getChatMessages());
            this.currentSong = new AtomicReference<>();
            this.scheduleSyncTimer();
            this.playNext(room.getCurrentSong());
            log.info("Initialized room instance {}", this);
        }

        @Transactional
        public void playNext() {
            RoomDAO roomDAO = roomDAOProvider.get();
            Room room = roomDAO.findById(id);
            List<Song> playQueue = room.getPlayQueue();
            if(!playQueue.isEmpty()) {
                playNext(playQueue.remove(0));
            }

            List<Song> playHistory = room.getPlayHistory();
            if(room.isRepeat()) {
                if(!playHistory.isEmpty()) {
                    playNext(playHistory.remove(0));
                }
                else {
                    playNext(currentSong.get().getSong());
                }
            }
            else {
                playNext(currentSong.get().getSong());
            }

            roomDAO.merge(room);
        }

        @Transactional
        protected void playNext(Song song) {
            final SongInstance songInstance = new SongInstance(song);
            currentSong.set(songInstance);
            log.info("Room {} now playing {}", this.id, song);
            ScheduledFuture<?> future = executorService.scheduleAtFixedRate(songInstance::incrementTime, 1l, 1l, TimeUnit.SECONDS);
            // Observer: Stop the increment time task when the song is finished
            songInstance.addObserver((o, arg) -> future.cancel(false));
            // Observer: Play the next song when the song is finished
            songInstance.addObserver((o, arg) -> playNext());
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
            chatMessage.setTimestamp(System.currentTimeMillis());

            messages.addLast(chatMessage);

            if (messages.size() > MAXIMAL_NUMBER_OF_CHAT_MESSAGES) {
                messages.removeFirst();
            }

            log.info("Sending message {} in room {}", chatMessage, this);
        }

        /**
         * Merge the changes of the instance in the database.
         */
        protected void merge() {
            log.info("Merging changes in room {}", this);
            performInUnitOfWork(() -> {
                RoomDAO roomDAO = roomDAOProvider.get();
                Room room = roomDAO.findById(id);
                room.setChatMessages(messages.stream().map(message -> {
                    ChatMessage chatMessage = new ChatMessage();
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

    public class SongInstance extends Observable {

        /**
         * The current time of the room's song.
         */
        private final AtomicLong currentTime;

        /**
         * The duration of the song
         */
        private final int duration;

        /**
         * Song id for the song
         */
        private final int songId;

        public SongInstance(Song song) {
            this.currentTime = new AtomicLong(0l);
            this.songId = song.getId();
            this.duration = song.getDuration();
        }

        @Transactional
        public Song getSong() {
            return songDAOProvider.get().findById(songId);
        }

        /**
         * Method used to increment the time of the current song by one second.
         */
        protected void incrementTime() {
            if(isStopped() && !hasChanged()) {
                log.debug("Song {} has finished playing", this);
                setChanged();
                notifyObservers();
            }
            else {
                log.debug("Incremented time for song {}", this);
                currentTime.addAndGet(200l);
            }
        }

        /**
         * Check if the song has completed
         * @return true if the song is finished
         */
        public boolean isStopped() {
            long time = currentTime.get();
            return time >= duration;
        }

        /**
         * Get the time
         * @return the time
         */
        public long getTime() {
            return currentTime.get();
        }

    }

}
