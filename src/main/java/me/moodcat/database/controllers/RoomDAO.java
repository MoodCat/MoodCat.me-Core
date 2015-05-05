package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QRoom.room;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Room;

public class RoomDAO extends AbstractDAO<Room> {

    protected RoomDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    public Room findById(final int id) {
        return this.query().from(room)
                .where(room.id.eq(id))
                .singleResult(room);
    }
}
