package me.moodcat.backend.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The backend of rooms, initializes room instances and keeps track of time and messages.
 */
@Slf4j
@Singleton
public class ChatBackend extends AbstractLifeCycleListener {

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
    private final Map<Integer, ChatRoomInstance> roomInstances;

    /**
     * The room DAO provider.
     */
    private final Provider<RoomDAO> roomDAOProvider;

    /**
     * The CallableInUnitOfWorkFactory which is used to perform large tasks in the background.
     */
    private final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory;

    /**
     * The constructor of the chat's backend, initializes fields and rooms.
     *
     * @param roomDAOProvider             the roomDAOProvider
     * @param callableInUnitOfWorkFactory the callableInUnitOfWorkFactory
     */
    @Inject
    public ChatBackend(Provider<RoomDAO> roomDAOProvider,
                       CallableInUnitOfWorkFactory callableInUnitOfWorkFactory) {
        this.executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        this.roomInstances = Maps.newHashMap();
        this.roomDAOProvider = roomDAOProvider;
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
        initializeInitialRooms();
    }

    /**
     * Initialize room instances for every room in the database.
     */
    public void initializeInitialRooms() {
        performInUnitOfWork(() -> {
            RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                    .map(ChatRoomInstance::new)
                    .forEach(room -> roomInstances.put(room.getRoom().getId(), room));
            return null;
        });
    }

    /**
     * Get a list of all rooms.
     *
     * @return a list of all rooms.
     */
    public List<Room> listAllRooms() {
        return Lists.newArrayList(roomInstances.values().stream().map(ChatRoomInstance::getRoom)
                .collect(Collectors.toList()));
    }

    /**
     * Get a room by its id.
     *
     * @param id the room's id
     * @return the room
     */
    public Room getRoom(int id) {
        return roomInstances.get(id).getRoom();
    }

    /**
     * Get a room instance by its id.
     *
     * @param id the room's id
     * @return the room
     */
    public ChatRoomInstance getRoomInstance(int id) {
        return roomInstances.get(id);
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {
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
    public class ChatRoomInstance {

        /**
         * The roomInstance's room.
         */
        private final Room room;

        /**
         * The current time of the room's song.
         */
        private final AtomicInteger currentTime;

        /**
         * ChatRoomInstance's constructur, will create a roomInstance from a room
         * and start the timer for the current song.
         *
         * @param room the room used to create the roomInstance.
         */
        public ChatRoomInstance(final Room room) {
            this.room = room;
            this.currentTime = new AtomicInteger(0);
            ChatBackend.this.executorService.scheduleAtFixedRate(this::incrementTime,
                    0, 1, TimeUnit.SECONDS);
            log.info("Created room {}", this);
        }

        /**
         * Store a message in the instance.
         *
         * @param chatMessage the message to send.
         */
        public void sendMessage(ChatMessage chatMessage) {
            chatMessage.setRoom(room);
            chatMessage.setTimestamp(System.currentTimeMillis());
            room.getChatMessages().add(chatMessage);
            merge();
            log.info("Sending message {} in room {}", chatMessage, this);
        }

        /**
         * Merge the changes of the instance in the database.
         */
        protected void merge() {
            log.info("Merging changes in room {}", this);
            performInUnitOfWork(() -> roomDAOProvider.get().merge(room));
        }

        /**
         * Get the instance's room.
         *
         * @return the room.
         */
        public Room getRoom() {
            return room;
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
         * Method used to make the room play the next song.
         */
        public void playNext() {
            List<Song> playHistory = room.getPlayHistory();
            playHistory.add(room.getCurrentSong());
            Song currentSong = null;

            List<Song> playQueue = getRoom().getPlayQueue();
            if (playQueue.isEmpty() && room.isRepeat()) {
                playQueue = Lists.newArrayList(playHistory);
            }

            if (!playQueue.isEmpty()) {
                currentSong = playQueue.remove(0);
            }

            log.info("Playing song {} in room {}", currentSong, this);
            room.setCurrentSong(currentSong);
            merge();
            resetTime();
        }

        /**
         * Method used to increment the time of the current song by one second.
         */
        protected void incrementTime() {
            System.out.println("Time incremented");
            final int time = this.currentTime.incrementAndGet();
            System.out.println("Time: " + time);
            final int duration = this.getCurrentSong().getDuration();
            if (time > duration) {
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
