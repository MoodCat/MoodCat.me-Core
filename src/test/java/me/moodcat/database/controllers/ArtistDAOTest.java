package me.moodcat.database.controllers;

import static org.junit.Assert.assertEquals;
import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.bootstrapper.BootstrapRule;
import me.moodcat.database.bootstrapper.TestBootstrap;
import me.moodcat.database.entities.Artist;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * This methods test to persist a Song
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class ArtistDAOTest {

    private final static String ARTIST_NAME = "Fall Out Boy New";

    /**
     * The ArtistDAO.
     */
    @Rule
    @Inject
    public BootstrapRule bootstrapRule;

    // Public for JUnit, it's required. Not unused either :)

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
        final Artist artist = new Artist();
        artist.setName(ARTIST_NAME);
        artistDAO.persist(artist);

        final Artist actual = artistDAO.findByName(ARTIST_NAME);
        assertEquals(artist, actual);
    }

    @Test
    @TestBootstrap("/bootstrap/artists.json")
    public void canRetrieveAllLists() {
        assertEquals(3, artistDAO.listArtists().size());
    }

    @Test
    @TestBootstrap("/bootstrap/artists.json")
    public void canRetrieveById() {
        assertEquals(2, artistDAO.findById(2).getId().intValue());
    }

}
