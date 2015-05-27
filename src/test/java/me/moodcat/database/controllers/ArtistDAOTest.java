package me.moodcat.database.controllers;

import com.google.inject.Inject;
import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.entities.Artist;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * This methods test to persist a Song
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class ArtistDAOTest {

    private final static String ARTIST_NAME = "Fall Out Boy";

    /**
     * The ArtistDAO.
     */
    @Inject
    private ArtistDAO artistDAO;

    /**
     * Persist a song with data.
     */
    @Test
    public void persistArtist() {
        Artist artist = new Artist();
        artist.setName(ARTIST_NAME);
        artistDAO.persist(artist);

        Artist actual = artistDAO.findByName(ARTIST_NAME);
        assertEquals(artist, actual);
    }

}
