package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QArtist.artist;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Artist;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Used to retrieve artists from the database.
 */
public class ArtistDAO extends AbstractDAO<Artist> {

    @Inject
    public ArtistDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Get all the artists stored in the database.
     *
     * @return A list of all artists.
     */
    @Transactional
    public List<Artist> listArtists() {
        return this.query().from(artist)
                .list(artist);
    }

    /**
     * Get an artist by name.
     *
     * @param name
     *            The (case-ignored) name of the artist.
     * @return The artist, if found.
     */
    @Transactional
    public Artist findByName(final String name) {
        return this.query().from(artist)
                .where(artist.name.equalsIgnoreCase(name))
                .singleResult(artist);
    }

    /**
     * Get an artist by id.
     *
     * @param id
     *            The id of the artist.
     * @return The artist, if found.
     */
    @Transactional
    public Artist findById(final int id) {
        return ensureExists(this.query().from(artist)
                .where(artist.id.eq(id))
                .singleResult(artist));
    }
}
