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
     * Retrieve random unclassified songs from the database.
     *
     * @param limit
     *            The number of songs to retrieve.
     * @return A list of random songs.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public List<Song> listRandomsongs(final int limit) {
        // QueryDSL does not support random ordering, so we have to make a custom native query.
        return this
                .getManager()
                .createNativeQuery(
                        "SELECT * FROM song WHERE arousal = 0"
                                + " AND valence = 0 ORDER BY RANDOM() LIMIT " + limit, Song.class)
                .getResultList();
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
        return ensureExists(this.query().from(song)
                .where(song.name.equalsIgnoreCase(name))
                .singleResult(song));
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
        return ensureExists(this.query().from(song)
                .where(song.id.eq(id))
                .singleResult(song));
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
        return ensureExists(this.query().from(song)
                .where(song.soundCloudId.eq(id))
                .singleResult(song));
    }
}
