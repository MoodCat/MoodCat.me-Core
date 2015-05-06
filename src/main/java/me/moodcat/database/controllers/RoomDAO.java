package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QRoom.room;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import me.moodcat.database.entities.Room;

import com.google.inject.Inject;

public class RoomDAO extends AbstractDAO<Room> {

    @Inject
    public RoomDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    @Transactional
    public Room findById(final int id) {
        return this.query().from(room)
                .where(room.id.eq(id))
                .singleResult(room);
    }

    public List<Room> listRooms() {
        return this.query().from(room).list(room);
    }
}
