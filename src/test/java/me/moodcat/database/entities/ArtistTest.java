package me.moodcat.database.entities;

import junitx.extensions.EqualsHashCodeTestCase;

import com.google.common.collect.Lists;

/**
 * @author Jaap Heijligers
 */
public class ArtistTest extends EqualsHashCodeTestCase {

    public ArtistTest(final String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        final Song song1 = new Song();
        song1.setName("First song");

        final Song song2 = new Song();
        song1.setName("Second song");

        final Artist artist = new Artist();
        artist.setName("ARTIST NAME");
        artist.setId(34);
        artist.setSongs(Lists.newArrayList(song1, song2));
        return artist;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        final Song song1 = new Song();
        song1.setName("First song");

        final Song song2 = new Song();
        song1.setName("Second song");

        final Artist artist = new Artist();
        artist.setName("ARTIST NAME");
        artist.setId(35);
        artist.setSongs(Lists.newArrayList(song1, song2));
        return artist;
    }

}
