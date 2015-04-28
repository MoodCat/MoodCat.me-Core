package me.moodcat.soundcloud;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SoundCloudSearch {

    /**
     * Search SoundCloud for tracks.
     *
     * @param query the query to search for
     * @return the search results
     * @throws IOException if the API response could not be downloaded
     */
    public ArrayList<SoundCloudTrack> search(String query) throws IOException {
        String searchUrl = "http://api.soundcloud.com/search?q=" + URLEncoder.encode(query, "UTF-8") + "&client_id="
                + SoundCloudExtract.CLIENT_ID;
        String page = IOUtils.toString(new URL(searchUrl));
        return extractApiRequestJson(page);
    }

    /**
     * Extract the JSON formatted response of a search API call on SoundCloud.
     *
     * @param jsonPage the JSON response
     * @return a list of parsed tracks
     */
    protected ArrayList<SoundCloudTrack> extractApiRequestJson(String jsonPage) {
        ArrayList<SoundCloudTrack> songArray = new ArrayList<>();
        JSONArray songs = new JSONObject(jsonPage).getJSONArray("collection");
        JSONObject song;
        for (int i = 0; i < songs.length(); i++) {
            song = songs.getJSONObject(i);
            if (song.getString("kind").equals("track")) {
                songArray.add(parseElement(song));
            }
        }
        return songArray;
    }

    /**
     * Parse a single track's JSON.
     *
     * @param element the JSONObject to parse
     * @return the parsed track
     */
    protected SoundCloudTrack parseElement(JSONObject element) {
        int id = element.getInt("id");
        String title = element.getString("title");
        String permalink = element.getString("permalink");
        String username = element.getJSONObject("user").getString("username");
        String artworkUrl = element.isNull("artwork_url") ? null : element.getString("artwork_url");
        int duration = element.getInt("duration");
        boolean downloadable = element.getBoolean("downloadable");
        return new
                SoundCloudTrack(id, title, permalink, username, artworkUrl, duration, downloadable);
    }

}
