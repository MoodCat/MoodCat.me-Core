package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QArtist.artist;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Artist;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * TODO: Add explanation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class ArtistDAO extends AbstractDAO<Artist> {

    @Inject
    public ArtistDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    @Transactional
    public List<Artist> listArtists() {
        return this.query().from(artist)
                .list(artist);
    }

    @Transactional
    public Artist findByName(final String name) {
        return this.query().from(artist)
                .where(artist.name.equalsIgnoreCase(name))
                .singleResult(artist);
    }

    @Transactional
    public Artist findById(final int id) {
        return this.query().from(artist)
                .where(artist.id.eq(id))
                .singleResult(artist);
    }
}
