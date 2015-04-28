package me.moodcat.soundcloud;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundCloudExtract {

    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    protected static final Pattern songPattern =
            Pattern.compile("https?://(www\\.)?soundcloud.com/(?<artist>.*?)/(?<permalink>.*?)$");

    /**
     * Retrieve a SoundCloudTrack given the artist and permalink.
     * @param artist the track's artist
     * @param permalink the track's permalink
     * @return the parsed {@link SoundCloudTrack}
     * @throws IOException when the download has failed
     */
    public SoundCloudTrack extract(String artist, String permalink) throws IOException {
        String infoUrl = resolveUrl(artist, permalink);
        return parseInfoJson(infoUrl);
    }

    /**
     * Retrieve a SoundCloudTrack given a SoundCloud URL.
     *
     * @param soundCloudUrl the give SoundCloud URL
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException if the URL is malformed
     */
    public SoundCloudTrack extract(String soundCloudUrl) throws IOException, SoundCloudException {
        Matcher matcher = songPattern.matcher(soundCloudUrl);
        if (matcher.find()) {
            String permalink = matcher.group("permalink");
            String artist = matcher.group("artist");
            return extract(artist, permalink);
        } else {
            throw new SoundCloudException("Wrong URL supplied");
        }
    }

    /**
     * Parse the stream URL of a not-downloadable SoundCloud track. This is done by downloading
     * and parsing a JSON response from the SoundCloud API.
     *
     * @param song the song to parse the stream URL of
     * @return the URL of the stream
     * @throws SoundCloudException if the stream could not be parsed
     * @throws IOException         if the mediaURL is malformed or if the JSON page
     *                             could not be downloaded.
     */
    protected String parseStreamUrl(SoundCloudTrack song)
            throws SoundCloudException, IOException {
        String streamJsonUrl = "http://api.soundcloud.com/i1/tracks/"
                + song.getId() + "/streams?client_id=" + CLIENT_ID
                + "&secret_token=None";

        String jsonPage = IOUtils.toString(new URL(streamJsonUrl));

        JSONObject root = new JSONObject(jsonPage);
        if (root.has("http_mp3_128_url")) {
            return root.getString("http_mp3_128_url");
        } else if (root.has("hls_mp3_128_url")) {
            return root.getString("hls_mp3_128_url");
        } else if (root.has("preview_mp3_128_url")) {
            return root.getString("preview_mp3_128_url");
        }
        throw new SoundCloudException("No stream URL found");
    }

    /**
     * Resolve the URL of the SoundCloud track given the artist and title.
     *
     * @param artist the artist
     * @param title  the title
     * @return the resolved URL
     */
    protected String resolveUrl(String artist, String title) {
        String url = null;
        try {
            url = "https://soundcloud.com/" + URLEncoder.encode(artist, "UTF-8") + "/" + URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "https://api.soundcloud.com/resolve.json?url=" + url + "&client_id=" + CLIENT_ID;
    }

    /**
     * Parse the information Json of the SoundCloud API.
     *
     * @param infoUrl the URL of the API request
     * @return the parsed {@link SoundCloudTrack}        System.out.println(searchUrl);
     * @throws IOException if the URL is malformed or could not be downloaded.
     */
    protected SoundCloudTrack parseInfoJson(String infoUrl) throws IOException {
        String jsonPage = IOUtils.toString(new URL(infoUrl));
        JSONObject root = new JSONObject(jsonPage);
        int id = root.getInt("id");
        String title = root.getString("title");
        String permalink = root.getString("permalink");
        String username = root.getJSONObject("user").getString("username");
        String artworkUrl = root.getString("artwork_url");
        int duration = root.getInt("duration");
        boolean downloadable = root.getBoolean("downloadable");
        return new SoundCloudTrack(id, title, permalink, username, artworkUrl,
                duration, downloadable);
    }

}
