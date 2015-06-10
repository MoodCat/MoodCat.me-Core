package me.moodcat.backend.rooms;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.RoomDAO;

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
     * UnitOfWorkSchedulingService.
     */
    private final UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    /**
     * RoomInstanceFactory for this roombackend.
     */
    private final RoomInstanceFactory roomInstanceFactory;

    /**
     * The room DAO provider.
     */
    private final Provider<RoomDAO> roomDAOProvider;

    /**
     * A map of room instances.
     */
    private final Map<Integer, RoomInstance> roomInstances;

    /**
     * The constructor of the chat's backend, initializes fields and rooms.
     *
     * @param unitOfWorkSchedulingService
     *          UnitOfWorkSchedulingService to schedule tasks in a unit of work
     * @param roomInstanceFactory
     *          RoomInstanceFactory to instantiate RoomInstances
     * @param roomDAOProvider
     *          Provider to create RoomDAOs when in a unit of work
     * @param lifeCycle
     *          The program lifecycle, to instantiate the initial rooms
     *          when the program has started
     */
    @Inject
    public RoomBackend(final UnitOfWorkSchedulingService unitOfWorkSchedulingService,
                       final RoomInstanceFactory roomInstanceFactory,
                       final Provider<RoomDAO> roomDAOProvider,
                       final LifeCycle lifeCycle) {
        this(unitOfWorkSchedulingService, roomInstanceFactory, roomDAOProvider);
        lifeCycle.addLifeCycleListener(this);
    }

    /**
     * The constructor of the chat's backend, initializes fields and rooms.
     *
     * @param unitOfWorkSchedulingService
     *          UnitOfWorkSchedulingService to schedule tasks in a unit of work
     * @param roomInstanceFactory
     *          RoomInstanceFactory to instantiate RoomInstances
     * @param roomDAOProvider
     *          Provider to create RoomDAOs when in a unit of work
     */
    public RoomBackend(final UnitOfWorkSchedulingService unitOfWorkSchedulingService,
                       final RoomInstanceFactory roomInstanceFactory,
                       final Provider<RoomDAO> roomDAOProvider) {
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;
        this.roomInstanceFactory = roomInstanceFactory;
        this.roomDAOProvider = roomDAOProvider;
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
        unitOfWorkSchedulingService.performInUnitOfWork(() -> {
            final RoomDAO roomDAO = roomDAOProvider.get();
            roomDAO.listRooms().stream()
                    .map(roomInstanceFactory::create)
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

}
