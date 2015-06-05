package me.moodcat.backend;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

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
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            final Provider<ChatDAO> chatDAOProvider) {
        this(roomDAOProvider, callableInUnitOfWorkFactory, Executors
                .newScheduledThreadPool(THREAD_POOL_SIZE), chatDAOProvider);
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
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            final ScheduledExecutorService executorService,
            final Provider<ChatDAO> chatDAOProvider) {
        this.executorService = executorService;
        this.roomDAOProvider = roomDAOProvider;
        this.chatDAOProvider = chatDAOProvider;
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
        this.roomInstances = initializeInitialRooms();
    }

    /**
     * Initialize room instances for every room in the database.
     */
    protected Map<Integer, RoomInstance> initializeInitialRooms() {
        return performInUnitOfWork(() -> {
            final RoomDAO roomDAO = roomDAOProvider.get();
            return roomDAO.listRooms().stream()
                    .collect(Collectors.toMap(Room::getId, RoomInstance::new));
        });
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

    @Override
    public void lifeCycleStopping(final LifeCycle event) {
        log.info("Shutting down executor for {}", this);
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
         * The number of seconds that the song is changed in order to keep up with the frontend.
         */
        private static final long NUMBER_OF_SECONDS_FOR_NEXT_SONG = 5;

        /**
         * The roomInstance's room.
         *
         * @return the room.
         */
        @Getter
        private final Room room;

        /**
         * The current time of the room's song.
         */
        private final AtomicInteger currentTime;

        /**
         * The cached messages in order to speed up retrieval.
         */
        private final LinkedList<ChatMessage> messages;

        /**
         * ChatRoomInstance's constructur, will create a roomInstance from a room
         * and start the timer for the current song.
         *
         * @param room
         *            the room used to create the roomInstance.
         */
        public RoomInstance(final Room room) {
            this.room = room;
            this.currentTime = new AtomicInteger(0);
            this.messages = new LinkedList<ChatMessage>(RoomBackend.this.chatDAOProvider.get()
                    .listByRoom(room));

            scheduleSongTimer();
            scheduleSyncTimer();
            log.info("Created room {}", this);
        }

        /**
         * Sync room messages to the database.
         */
        private void scheduleSyncTimer() {
            this.room.setChatMessages(Lists.newArrayList(messages));
            RoomBackend.this.executorService.scheduleAtFixedRate(this::merge,
                    0, 1, TimeUnit.MINUTES);
        }

        /**
         * Schedule song timer.
         */
        private void scheduleSongTimer() {
            RoomBackend.this.executorService.scheduleAtFixedRate(this::incrementTime,
                    0, 1, TimeUnit.SECONDS);
        }

        /**
         * Store a message in the instance.
         *
         * @param chatMessage
         *            the message to send.
         */
        public void sendMessage(final ChatMessage chatMessage) {
            chatMessage.setRoom(room);
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
            performInUnitOfWork(() -> roomDAOProvider.get().merge(room));
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
        public Song getCurrentSong() {
            return this.room.getCurrentSong();
        }

        /**
         * Get the progress of the current song.
         *
         * @return the progress in seconds.
         */
        public int getCurrentTime() {
            return this.currentTime.get();
        }

        /**
         * Get the room name.
         *
         * @return the name of the room
         */
        public String getName() {
            return this.room.getName();
        }

        /**
         * Method used to make the room play the next song.
         */
        public void playNext() {
            final List<Song> playHistory = room.getPlayHistory();
            playHistory.add(room.getCurrentSong());
            List<Song> playQueue = room.getPlayQueue();

            if (playQueue.isEmpty()) {
                if (room.isRepeat()) {
                    playQueue = Lists.newArrayList(playHistory);
                } else {
                    // TODO: Select songs from a database.
                }
            }

            if (!playQueue.isEmpty()) {
                final Song currentSong = playQueue.remove(0);
                log.info("Playing song {} in room {}", currentSong, this);
                room.setCurrentSong(currentSong);
                merge();
            }

            resetTime();
        }

        /**
         * Method used to increment the time of the current song by one second.
         */
        protected void incrementTime() {
            final int time = this.currentTime.incrementAndGet();
            final int duration = (int) TimeUnit.MILLISECONDS.toSeconds(this.getCurrentSong()
                    .getDuration());

            if (duration - time < NUMBER_OF_SECONDS_FOR_NEXT_SONG) {
                playNext();
            }
        }

        /**
         * Reset the time of the room's current song.
         */
        protected void resetTime() {
            log.debug("Resetting time counter in room {}", this);
            this.currentTime.set(0);
        }
    }
}
