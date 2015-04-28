package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QSong.song;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Song;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * TODO: Add explanation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class SongDAO extends AbstractDAO<Song> {

    @Inject
    public SongDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    @Transactional
    public List<Song> listSongs() {
        return this.query().from(song)
                .list(song);
    }

    @Transactional
    public Song findByName(final String name) {
        return this.query().from(song)
                .where(song.name.equalsIgnoreCase(name))
                .singleResult(song);
    }

    @Transactional
    public Song findById(final int id) {
        return this.query().from(song)
                .where(song.id.eq(id))
                .singleResult(song);
    }
}
