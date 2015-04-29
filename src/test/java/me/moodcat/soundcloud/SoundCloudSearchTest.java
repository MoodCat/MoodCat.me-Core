package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudSearchTest extends SoundCloudAPIConnectorTest {

    private static final int NUMBER_OF_SONGS = 10;

    private static final String SEARCH_CONTENT;

    static {
        final StringBuilder builder = new StringBuilder();

        builder.append("{\"collection\":[");

        for (int i = 0; i < NUMBER_OF_SONGS; i++) {
            builder.append(SONG_JSON_REPRESENTATION).append(',');
        }

        builder.deleteCharAt(builder.length() - 1);

        builder.append("]}");

        SEARCH_CONTENT = builder.toString();
    }

    private SoundCloudSearch search;

    /**
     * Setup {@link #search}.
     */
    @Before
    public void setUp() {
        this.search = new SoundCloudSearch();

        this.setUp(this.search, SEARCH_CONTENT);
    }

    @Test
    public void testSearch() throws IOException {
        try {
            final List<SoundCloudTrack> searchResults = this.search.search("Katfyr");

            assertEquals(NUMBER_OF_SONGS, searchResults.size());
        } catch (final SoundCloudException e) {
            fail();
        }
    }

}
