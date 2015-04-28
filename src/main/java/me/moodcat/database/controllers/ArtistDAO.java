package me.moodcat.database.controllers;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import me.moodcat.database.entities.Artist;
import static me.moodcat.database.entities.QArtist.artist;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ArtistDAO extends AbstractDAO<Artist> {

    @Inject
    public ArtistDAO(EntityManager entityManager) {
        super(entityManager);
    }

    @Transactional
    public List<Artist> listArtists() {
        return query().from(artist)
            .list(artist);
    }

    @Transactional
    public Artist findByName(String name) {
        return query().from(artist)
            .where(artist.name.equalsIgnoreCase(name))
            .singleResult(artist);
    }

    @Transactional
    public Artist findById(int id) {
        return query().from(artist)
            .where(artist.id.eq(id))
            .singleResult(artist);
    }
}
