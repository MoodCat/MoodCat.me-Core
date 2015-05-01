package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by jaap on 5/1/15.
 */
public class SoundCloudIntegrationTest {

    final static String TRACK_URL = "https://soundcloud.com/katfyr/binary-original-mix";

    final static String TRACK_USER = "KATFYR";

    final static String TRACK_PERMALINK = "binary-original-mix";

    final static String TRACK_TITLE = "Binary (Original Mix)";

    final static int TRACK_ID = 101712416;

    final static String TRACK_ARTWORK_URL = "https://i1.sndcdn.com/artworks-000053288547-j50pwp-large.jpg";

    final static int TRACK_DURATION = 283793;

    final static boolean TRACK_DOWNLOADABLE = false;

    static SoundCloudTrack testTrack;

    @BeforeClass
    public static void initializeTrack1() throws SoundCloudException {
        SoundCloudExtract soundCloudExtract = new SoundCloudExtract();
        testTrack = soundCloudExtract.extract(TRACK_URL);
    }

    @Test
    public void testSearch() throws SoundCloudException {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        SoundCloudSearch search = new SoundCloudSearch();
        List<SoundCloudTrack> tracks = search.search("Katfyr");
        assertTrue(tracks.size() > 10);
        for (SoundCloudTrack track : tracks) {
            assertTrue(track.getId() > 0);
            assertTrue(track.getPermalink() != null);
        }
    }

    @Test
    public void testExtractTitle() throws SoundCloudException {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getTitle(), TRACK_TITLE);
    }

    @Test
    public void testExtractPermalink() throws SoundCloudException {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getPermalink(), TRACK_PERMALINK);
    }

    @Test
    public void testExtractUser() throws SoundCloudException {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getUser().getUsername(), TRACK_USER);
    }

    @Test
    public void testExtractId() {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getId(), TRACK_ID);
    }

    @Test
    public void testArtworkUrl() {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getArtworkUrl(), TRACK_ARTWORK_URL);
    }

    @Test
    public void testDuration() {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.getDuration(), TRACK_DURATION);
    }

    @Test
    public void testDownloadable() {
        Assume.assumeTrue("true".equals(System.getProperty("runWithIntegration")));
        assertEquals(testTrack.isDownloadable(), TRACK_DOWNLOADABLE);
    }
}
