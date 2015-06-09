package me.moodcat.database.entities;

import junitx.extensions.EqualsHashCodeTestCase;
import me.moodcat.database.embeddables.VAVector;

/**
 * @author Jaap Heijligers
 */
public class SongTest extends EqualsHashCodeTestCase {

    public SongTest(final String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        final Song song = new Song();
        song.setName("Song name");
        final VAVector va = new VAVector(0.4, 0.5);
        song.setValenceArousal(va);
        song.setArtworkUrl("http://artwork.url/");
        song.setId(43);
        song.setSoundCloudId(273642387);
        song.setDuration(3423);

        return song;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        final Song song = new Song();
        song.setName("Song name");
        final VAVector va = new VAVector(0.4, 0.5);
        song.setValenceArousal(va);
        song.setArtworkUrl("http://artwork.url/");
        song.setId(44);
        song.setSoundCloudId(273642387);
        song.setDuration(3423);

        return song;
    }

}
