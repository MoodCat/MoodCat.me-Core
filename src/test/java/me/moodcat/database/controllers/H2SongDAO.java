package me.moodcat.database.controllers;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;

import com.google.inject.Inject;

public class H2SongDAO extends SongDAO {

	@Inject
	public H2SongDAO(EntityManager entityManager) {
		super(entityManager);
	}
	
	@Override
	public List<Song> findForDistance(VAVector vector, int limit) {
		return super.listRandomsongs(limit);
	}

}
