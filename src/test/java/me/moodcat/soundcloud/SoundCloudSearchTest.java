package me.moodcat.soundcloud;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudSearchTest {

    private SoundCloudSearch search;

    @Before
    public void setUp() {
        this.search = new SoundCloudSearch();
    }

    @Test
    public void testSearch() throws SoundCloudException {
        final List<SoundCloudTrack> searchResults = this.search.search("Katfyr");
        assertTrue(searchResults.size() > 10);
    }

}
