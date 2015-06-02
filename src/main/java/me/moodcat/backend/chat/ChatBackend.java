package me.moodcat.backend.chat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * The backend of rooms, initializes room instances and keeps track of time and messages.
 */
@Slf4j
@Singleton
public class ChatBackend extends AbstractLifeCycleListener {

    private final ScheduledExecutorService executorService;

    private final Map<Integer, ChatRoomInstance> roomInstances;

    private final Provider<RoomDAO> roomDAOProvider;

    private final Provider<ChatDAO> chatDAOProvider;

    private final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory;

    @Inject
    public ChatBackend(Provider<RoomDAO> roomDAOProvider,
            CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            Provider<ChatDAO> chatDAOProvider) {
        this.executorService = Executors.newScheduledThreadPool(4);
        this.roomInstances = Maps.newHashMap();
        this.roomDAOProvider = roomDAOProvider;
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
        this.chatDAOProvider = chatDAOProvider;
        initializeInitialRooms();
    }

    public void initializeInitialRooms() {
        performInUnitOfWork(() -> {
            RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                    .map(ChatRoomInstance::new)
                    .forEach(room -> roomInstances.put(room.getRoom().getId(), room));
            List<ChatMessage> msgs = roomDAO.listRooms().get(0).getChatMessages();
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
     * Get a list of all rooms instances.
     *
     * @return a list of all rooms instances.
     */
    public Collection<ChatRoomInstance> listAllRoomsInstances() {
        return roomInstances.values();
    }

    /**
     * Get a room by its id.
     * 
     * @param id
     *            the room's id
     * @return the room
     */
    public Room getRoom(int id) {
        return roomInstances.get(id).getRoom();
    }

    /**
     * Get a room instance by its id.
     * 
     * @param id
     *            the room's id
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

    public class ChatRoomInstance {

        private final Room room;

        private final AtomicInteger currentTime;

        public ChatRoomInstance(final Room room) {
            this.room = room;
            this.currentTime = new AtomicInteger(0);
            ChatBackend.this.executorService.scheduleAtFixedRate(this::incrementTime, 0, 1, TimeUnit.SECONDS);
            log.info("Created room {}", this);
        }

        public void sendMessage(ChatMessage chatMessage) {
            chatMessage.setRoom(room);
            chatMessage.setTimestamp(System.currentTimeMillis());
            room.getChatMessages().add(chatMessage);
            merge();
            log.info("Sending message {} in room {}", chatMessage, this);
        }

        protected void merge() {
            log.info("Merging changes in room {}", this);
            performInUnitOfWork(() -> roomDAOProvider.get().merge(room));
        }

        public Room getRoom() {
            return room;
        }

        public Song getCurrentSong() {
            return this.room.getCurrentSong();
        }

        public int getCurrentTime() {
            return this.currentTime.get();
        }

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

        protected void incrementTime() {
            System.out.println("Time incremented");
            final int currentTime = this.currentTime.incrementAndGet();
            System.out.println("Time: " + currentTime);
            final int duration = this.getCurrentSong().getDuration();
            if (currentTime > duration) {
                playNext();
            }
        }

        protected void resetTime() {
            log.debug("Resetting time counter in room {}", this);
            this.currentTime.set(0);
        }

    }
}
