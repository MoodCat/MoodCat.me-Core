package me.moodcat.database.controllers;

import com.google.inject.Inject;
import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import static org.junit.Assert.assertEquals;

/**
 * This methods test to persist a Song
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class SongDAOTest {

    private final static String ARTIST_NAME = "Fall Out Boy";
    private final static String SONG_NAME = "Thanks for the Memories";

    /**
     * The ArtistDAO.
     */
    @Inject
    private ArtistDAO artistDAO;

    /**
     * The SongDAO.
     */
    @Inject
    private SongDAO songDAO;

    /**
     * The artist.
     */
    private Artist artist;

    /**
     * Create artist.
     */
    @Before
    public void createArist() {
        artist = new Artist();
        artist.setName(ARTIST_NAME);
        artistDAO.persist(artist);

        Artist actual = artistDAO.findByName(ARTIST_NAME);
        assertEquals(artist, actual);
    }

    /**
     * Persist a song with data.
     */
    @Test
    public void persistSongWithData() {
        final Song song = new Song();
        song.setName(SONG_NAME);
        song.setArtist(artist);
        songDAO.persist(song);

        Song actual = songDAO.findByName(SONG_NAME);
        assertEquals(song, actual);
    }

}
