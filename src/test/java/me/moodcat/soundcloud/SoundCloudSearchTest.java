package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudSearchTest extends SoundCloudAPIConnectorTest {

    private static final int NUMBER_OF_SONGS = 10;

    private static final String SEARCH_URL = "http://api.soundcloud.com/search?q=Katfyr&client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

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
        final ArrayList<SoundCloudTrack> searchResults = this.search.search("Katfyr");

        assertEquals(NUMBER_OF_SONGS, searchResults.size());
    }

    @Test
    public void testExtractApiRequestJson() throws IOException {
        final String searchPage = this.factory.getContent(SEARCH_URL);

        final ArrayList<SoundCloudTrack> results = this.search.extractApiRequestJson(searchPage);
        assertEquals(NUMBER_OF_SONGS, results.size());
    }

    @Test
    public void testParseElement() throws IOException {
        final String searchPage = this.factory.getContent(SEARCH_URL);
        final JSONObject object = new JSONObject(searchPage);
        final JSONObject trackObject = object.getJSONArray("collection").getJSONObject(0);

        final SoundCloudTrack track = this.search.parseTrack(trackObject);

        assertEquals(trackObject.getString("title"), track.getTitle());
    }
}
