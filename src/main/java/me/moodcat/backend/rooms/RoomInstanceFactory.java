package me.moodcat.backend.rooms;

import me.moodcat.database.entities.Room;

/**
 * Created by jwgmeligmeyling on 9-6-15.
 */
public interface RoomInstanceFactory {

    /**
     * Create a new {@link RoomInstance}.
     *
     * @param room
     * @return
     */
    RoomInstance create(Room room);

}
