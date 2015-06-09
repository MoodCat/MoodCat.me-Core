package me.moodcat.backend.rooms;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.common.collect.Maps;
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
    protected final ScheduledExecutorService executorService;

    /**
     * The room DAO provider.
     */
    protected final Provider<RoomDAO> roomDAOProvider;

    /**
     * A map of room instances.
     */
    private final Map<Integer, RoomInstance> roomInstances;

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
    
    public SongDAO getSongDAO() {
    	return this.songDAOProvider.get();
    }

    /**
     * Initialize the rooms from the db.
     */
    public void initializeRooms() {
        performInUnitOfWork(() -> {
            final RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                    .map((room) -> new RoomInstance(this, room))
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

}
