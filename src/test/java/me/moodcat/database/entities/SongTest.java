package me.moodcat.database.entities;

import static org.junit.Assert.assertEquals;
import me.moodcat.database.embeddables.VAVector;

import org.junit.Test;

/**
 * @author Jaap Heijligers
 */
public class SongTest {

    @Test
    public void songTest() {
        final Song song1 = createDefaultSong();
        final Song song2 = createDefaultSong();
        assertEquals(song1, song2);
    }

    private Song createDefaultSong() {
        final Song song = new Song();
        song.setName("Song name");
        final VAVector va = new VAVector(0.4, 0.5);
        song.setValenceArousal(va);
        song.setArtworkUrl("http://artwork.url/");
        song.setId(43);
        song.setSoundCloudId(273642387);
        song.setDuration(3423);
        song.setNumberOfPositiveVotes(5);

        return song;
    }

}
