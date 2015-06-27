package me.moodcat.backend.rooms;

/**
 * Created by jwgmeligmeyling on 9-6-15.
 */
public interface RoomInstanceInUnitOfWorkFactory {

    /**
     * Create a new {@link RoomInstance}.
     *
     * @param id
     *            Room to instantiate a RoomInstanceInUnitOfWork for.
     * @return instantiated RoomInstance.
     */
    RoomInstanceInUnitOfWork create(Integer id);

}
