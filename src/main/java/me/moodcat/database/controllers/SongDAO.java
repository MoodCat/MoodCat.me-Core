package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QSong.song;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Song;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Used to retrieve songs from the database.
 */
public class SongDAO extends AbstractDAO<Song> {

    @Inject
    public SongDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Get all the lists stored in the database.
     *
     * @return The list of songs stored in the database.
     */
    @Transactional
    public List<Song> listSongs() {
        return this.query().from(song)
                .list(song);
    }

    /**
     * Get a song by name.
     *
     * @param name
     *            The (case-ignored) name of the song.
     * @return The song, if found.
     */
    @Transactional
    public Song findByName(final String name) {
        return this.query().from(song)
                .where(song.name.equalsIgnoreCase(name))
                .singleResult(song);
    }

    /**
     * Get a song by id.
     *
     * @param id
     *            The id of song.
     * @return The song, if found.
     */
    @Transactional
    public Song findById(final int id) {
        return this.query().from(song)
                .where(song.id.eq(id))
                .singleResult(song);
    }

    /**
     * Get a song by SoundCloud id.
     *
     * @param id
     *            The SoundCloud id of the song.
     * @return The song, if found.
     */
    @Transactional
    public Song findBySoundCloudId(final int id) {
        return this.query().from(song)
                .where(song.soundCloudId.eq(id))
                .singleResult(song);
    }
}
