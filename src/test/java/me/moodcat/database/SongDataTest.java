package me.moodcat.database;

import java.io.IOException;
import java.io.InputStream;

import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.AcousticBrainzData;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * This methods test to persist a Song with AcousicBrainzData attached
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class SongDataTest {

    @Inject
    private ArtistDAO artistDAO;

    @Inject
    private SongDAO songDAO;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void persistSongWithData() throws IOException {
        final Artist fallOutBoy = new Artist();
        fallOutBoy.setName("Fall Out Boy");
        this.artistDAO.persist(fallOutBoy);

        final Song song = new Song();
        song.setName("Thanks for the Memories");
        song.setArtist(fallOutBoy);

        try (InputStream in = SongDataTest.class
                .getResourceAsStream("/acousticbrainz/testData/32098913.json")) {
            final AcousticBrainzData features = this.objectMapper.readValue(in,
                    AcousticBrainzData.class);
            song.setFeatures(features);
        }

        song.setExpectedValenceArousal(new VAVector(.76, .78));
        this.songDAO.persist(song);
    }

}
