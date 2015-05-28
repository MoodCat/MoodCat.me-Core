package me.moodcat.backend.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Song;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import me.moodcat.database.entities.Room;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jan-Willem Gmelig Meyling
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
    }

    public void initializeInitialRooms() {
        performInUnitOfWork(() -> {
            RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                .map(ChatRoomInstance::new)
                .forEach(room -> roomInstances.put(room.getRoom().getId(), room));
            return null;
        });
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
            ChatBackend.this.executorService.schedule(this::incrementTime, 1, TimeUnit.SECONDS);
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
            if(playQueue.isEmpty() && room.isRepeat()) {
                playQueue = Lists.newArrayList(playHistory);
            }

            if(!playQueue.isEmpty()) {
                currentSong = playQueue.remove(0);
            }

            log.info("Playing song {} in room {}", currentSong, this);
            room.setCurrentSong(currentSong);
            merge();
            resetTime();
        }

        protected void incrementTime() {
            final int currentTime = this.currentTime.incrementAndGet();
            final int duration = this.getCurrentSong().getDuration();
            if(currentTime > duration) {
                playNext();
            }
        }

        protected void resetTime() {
            log.debug("Resetting time counter in room {}", this);
            this.currentTime.set(0);
        }

    }
}
