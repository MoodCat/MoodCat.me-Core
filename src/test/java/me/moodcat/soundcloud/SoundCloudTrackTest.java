package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudTrackTest {

    private SoundCloudTrack track;

    @Before
    public void setUp() {
        this.track = new SoundCloudTrack(159518,
                "Mom's Spaghetti - Eminem', permalink='moms-spaghetti-eminem",
                "moms-spaghetti-eminem", "Campbell Logan 1",
                "https://i1.sndcdn.com/artworks-000038396790-h5ybh8-large.jpg", 159518, true);
    }

    @Test
    public void testHasStreamUrl() {
        assertFalse(this.track.hasStreamUrl());
    }

    @Test
    public void testGetStreamUrl() {
        assertNull(this.track.getStreamUrl());
    }

    @Test
    public void testGenerateStreamUrl() throws IOException, SoundCloudException {
        assertFalse(this.track.hasStreamUrl());
        this.track.generateStreamUrl();
        assertTrue(this.track.hasStreamUrl());
        try {
            assertNotNull(new URL(this.track.getStreamUrl()));
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetArtworkUrl() throws MalformedURLException {
        assertNotNull(new URL(this.track.getArtworkUrl()));
    }

    @Test
    public void testIsDownloadable() {
        assertTrue(this.track.isDownloadable());
    }

    @Test
    public void testGetId() {
        assertEquals(this.track.getId(), 159518);
    }

    @Test
    public void testGetTitle() {
        assertEquals(this.track.getTitle(),
                "Mom's Spaghetti - Eminem', permalink='moms-spaghetti-eminem");
    }
}
