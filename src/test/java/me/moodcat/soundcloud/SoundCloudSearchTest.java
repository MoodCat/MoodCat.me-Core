package me.moodcat.soundcloud;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudSearchTest {

    final static String SEARCH_URL = "http://api.soundcloud.com/search?q=Katfyr&client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

    @Test
    public void testSearch() throws Exception {
        SoundCloudSearch search = new SoundCloudSearch();
        ArrayList<SoundCloudTrack> searchResults = search.search("Katfyr");
        assertTrue(searchResults.size() > 10);
    }

    @Test
    public void testExtractApiRequestJson() throws Exception {
        String searchPage = IOUtils.toString(new URL(SEARCH_URL).openStream());
        SoundCloudSearch search = new SoundCloudSearch();
        ArrayList<SoundCloudTrack> results = search.extractApiRequestJson(searchPage);
        assertTrue(results.size() > 10);
    }

    @Test
    public void testParseElement() throws Exception {
        String searchPage = IOUtils.toString(new URL(SEARCH_URL).openStream());
        JSONObject object = new JSONObject(searchPage);
        JSONObject trackObject = object.getJSONArray("collection").getJSONObject(0);
        SoundCloudTrack track = new SoundCloudSearch().parseElement(trackObject);
        assertEquals(trackObject.getString("title"), track.getTitle());
    }
}