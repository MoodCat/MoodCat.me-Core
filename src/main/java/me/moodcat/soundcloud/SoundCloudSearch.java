package me.moodcat.soundcloud;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class SoundCloudSearch extends SoundCloudAPIConnector {

    /**
     * Search SoundCloud for tracks.
     *
     * @param query
     *            the query to search for
     * @return the search results
     * @throws IOException
     *             if the API response could not be downloaded
     */
    public ArrayList<SoundCloudTrack> search(final String query) throws IOException {
        final String searchUrl = "http://api.soundcloud.com/search?q="
                + URLEncoder.encode(query, "UTF-8") + "&client_id=" + SoundCloudExtract.CLIENT_ID;

        final String page = this.getUrlFactory().getContent(searchUrl);

        return this.extractApiRequestJson(page);
    }

    /**
     * Extract the JSON formatted response of a search API call on SoundCloud.
     *
     * @param jsonPage
     *            the JSON response
     * @return a list of parsed tracks
     */
    protected ArrayList<SoundCloudTrack> extractApiRequestJson(final String jsonPage) {
        final ArrayList<SoundCloudTrack> songArray = new ArrayList<>();
        final JSONArray songs = new JSONObject(jsonPage).getJSONArray("collection");
        JSONObject song;

        for (int i = 0; i < songs.length(); i++) {
            song = songs.getJSONObject(i);

            if (song.getString("kind").equals("track")) {
                songArray.add(this.parseTrack(song));
            }
        }
        return songArray;
    }

}
