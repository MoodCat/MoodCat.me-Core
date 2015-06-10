package me.moodcat.database.controllers;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Room;

import com.google.inject.Inject;

public class H2RoomDAO extends RoomDAO {

	@Inject
	public H2RoomDAO(EntityManager entityManager) {
		super(entityManager);
	}
	
	@Override
	public List<Room> queryRooms(VAVector vector, int limit) {
		return super.listRooms(limit);
	}

}
