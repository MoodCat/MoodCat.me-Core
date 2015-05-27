package me.moodcat.database.controllers;

import com.google.inject.Inject;
import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.bootstrapper.BootstrapRule;
import me.moodcat.database.bootstrapper.TestBootstrap;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * This methods test to persist a Song
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class SongDAOTest {

    private final static Integer EXISTING_SONG_ID = 1;
    private final static Integer EXISTING_SONG_SOUNDCLOUD_ID = 202330997;
    private final static String NEW_SONG_NAME = "Thriller";
    private final static String EXISTING_SONG_NAME = "Thanks for the Memories";

    /**
     * The ArtistDAO.
     */
    @Rule
    @Inject
    public BootstrapRule bootstrapRule;
    // Public for JUnit, it's required. Not unused either :)

    /**
     * The SongDAO.
     */
    @Inject
    private SongDAO songDAO;

    /**
     * The artist to interact with
     */
    private Artist artist;

    @Before
    public void setArtist() {
        artist = bootstrapRule.getFirstArtist();
    }

    /**
     * Query a song inserted by the bootstrap.
     * The query should not fail and the result entity
     * should have the expected name.
     */
    @Test
    @TestBootstrap("/bootstrap/fall-out-boy.json")
    public void queryExistingSongByName() {
        Song actual = songDAO.findByName(EXISTING_SONG_NAME);
        assertEquals(EXISTING_SONG_NAME, actual.getName());
    }

    /**
     * Query all songs returns a list of songs
     */
    @Test
    @TestBootstrap("/bootstrap/fall-out-boy.json")
    public void listSongTests() {
        List<Song> songs = songDAO.listSongs();
        Song expected = songDAO.findById(EXISTING_SONG_ID);
        assertThat(songs, hasItem(expected));
    }

    /**
     * Query a song inserted by the bootstrap.
     * The query should not fail and the result entity
     * should have the expected id.
     */
    @Test
    @TestBootstrap("/bootstrap/fall-out-boy.json")
    public void queryExistingSongById() {
        Song actual = songDAO.findById(EXISTING_SONG_ID);
        assertEquals(EXISTING_SONG_ID, actual.getId());
    }

    /**
     * Query a song inserted by the bootstrap.
     * The query should not fail and the result entity
     * should have the expected name.
     */
    @Test
    @TestBootstrap("/bootstrap/fall-out-boy.json")
    public void queryExistingSongBySoundcloudId() {
        Song actual = songDAO.findBySoundCloudId(EXISTING_SONG_SOUNDCLOUD_ID);
        assertEquals(EXISTING_SONG_SOUNDCLOUD_ID, actual.getSoundCloudId());
    }

    /**
     * Persist a song with data.
     * Verify that it can be queried afterwards.
     */
    @Test
    @TestBootstrap("/bootstrap/fall-out-boy.json")
    public void persistSong() {
        final Song song = new Song();
        song.setName(NEW_SONG_NAME);
        song.setArtist(artist);
        songDAO.persist(song);

        Song actual = songDAO.findByName(NEW_SONG_NAME);
        assertEquals(song, actual);
    }

}
