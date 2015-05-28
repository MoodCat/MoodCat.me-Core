package me.moodcat.database.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author Jaap Heijligers
 */
public class ArtistTest {

    @Test
    public void testArtist() {
        Artist artist1 = createDefaultArist();
        Artist artist2 = createDefaultArist();
        assertEquals(artist1, artist2);
    }

    private Artist createDefaultArist() {
        Song song1 = new Song();
        song1.setName("First song");

        Song song2 = new Song();
        song1.setName("Second song");

        Artist artist = new Artist();
        artist.setName("ARTIST NAME");
        artist.setId(34);
        artist.setSongs(Lists.newArrayList(song1, song2));
        return artist;
    }

}
