package me.moodcat.soundcloud;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudTrackTest {

    private SoundCloudTrack track;

    @Before
    public void setUp() throws Exception {
        track = new SoundCloudTrack(159518,
                "Mom's Spaghetti - Eminem', permalink='moms-spaghetti-eminem",
                "moms-spaghetti-eminem",
                "Campbell Logan 1",
                "https://i1.sndcdn.com/artworks-000038396790-h5ybh8-large.jpg",
                159518,
                true);
    }

    @Test
    public void testHasStreamUrl() throws Exception {
        assertFalse(track.hasStreamUrl());
    }

    @Test
    public void testGetStreamUrl() throws Exception {
        assertNull(track.getStreamUrl());
    }

    @Test
    public void testGenerateStreamUrl() throws Exception {
        assertFalse(track.hasStreamUrl());
        track.generateStreamUrl();
        assertTrue(track.hasStreamUrl());
        assertNotNull(new URL(track.getStreamUrl()));
    }

    @Test
    public void testGetArtworkUrl() throws Exception {
        assertNotNull(new URL(track.getArtworkUrl()));
    }

    @Test
    public void testIsDownloadable() throws Exception {
        assertTrue(track.isDownloadable());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(track.getId(), 159518);
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(track.getTitle(), "Mom's Spaghetti - Eminem', permalink='moms-spaghetti-eminem");
    }
}