package me.moodcat.database.controllers;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;

import javax.persistence.EntityManager;
import java.util.List;

import static me.moodcat.database.entities.QSong.song;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class SongDAO extends AbstractDAO<Song> {

    @Inject
    public SongDAO(EntityManager entityManager) {
        super(entityManager);
    }

    @Transactional
    public List<Song> listSongs() {
        return query().from(song)
                .list(song);
    }

    @Transactional
    public Song findByName(String name) {
        return query().from(song)
            .where(song.name.equalsIgnoreCase(name))
            .singleResult(song);
    }

    @Transactional
    public Song findById(int id) {
        return query().from(song)
            .where(song.id.eq(id))
            .singleResult(song);
    }
}
