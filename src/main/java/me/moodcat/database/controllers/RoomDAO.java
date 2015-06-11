package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QRoom.room;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Room;

import com.google.inject.Inject;

/**
 * The DAO for rooms.
 */
public class RoomDAO extends AbstractDAO<Room> {

    @Inject
    public RoomDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Find a room by id.
     *
     * @param id
     *            the id of the room
     * @return the {@link Room}
     */
    @Transactional
    public Room findById(final int id) {
        return ensureExists(this.query().from(room)
                .where(room.id.eq(id))
                .singleResult(room));
    }

    /**
     * Obtain all the rooms from the database.
     *
     * @return The list of all rooms.
     */
    @Transactional
    public List<Room> listRooms() {
        return this.query().from(room).list(room);
    }

    /**
     * Obtain all the rooms from the database limited to lim.
     *
     * @param lim
     *            The number of rooms to return.
     * @return The list of lim rooms.
     */
    @Transactional
    public List<Room> listRooms(final int lim) {
        return this.query().from(room).limit(lim).list(room);
    }

    /**
     * Query rooms in space.
     * 
     * @param vector
     *            Vector to compare with
     * @param limit
     *            Limit results
     * @return a list of rooms
     */
    @Transactional
    public List<Room> queryRooms(final VAVector vector, final int limit) {
        return query().from(room)
                .orderBy(room.vaVector.location.distance(vector.getLocation()).asc())
                .limit(limit)
                .list(room);
    }

}
