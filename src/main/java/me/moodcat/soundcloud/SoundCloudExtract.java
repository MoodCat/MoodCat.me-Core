package me.moodcat.soundcloud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class SoundCloudExtract extends SoundCloudAPIConnector {

    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    protected static final Pattern songPattern = Pattern
            .compile("https?://(www\\.)?soundcloud.com/(?<artist>.*?)/(?<permalink>.*?)$");

    /**
     * Retrieve a SoundCloudTrack given the artist and permalink.
     *
     * @param artist
     *            the track's artist
     * @param permalink
     *            the track's permalink
     * @return the parsed {@link SoundCloudTrack}
     * @throws IOException
     *             when the download has failed
     */
    public SoundCloudTrack extract(final String artist, final String permalink) throws IOException {
        final String infoUrl = this.resolveUrl(artist, permalink);

        return this.parseInfoJson(infoUrl);
    }

    /**
     * Retrieve a SoundCloudTrack given a SoundCloud URL.
     *
     * @param soundCloudUrl
     *            the give SoundCloud URL
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException
     *             if the URL is malformed
     */
    public SoundCloudTrack extract(final String soundCloudUrl) throws IOException,
            SoundCloudException {
        final Matcher matcher = songPattern.matcher(soundCloudUrl);

        if (matcher.find()) {
            final String permalink = matcher.group("permalink");
            final String artist = matcher.group("artist");

            return this.extract(artist, permalink);
        }

        throw new SoundCloudException("Wrong URL supplied");
    }

    /**
     * Parse the stream URL of a not-downloadable SoundCloud track. This is done
     * by downloading and parsing a JSON response from the SoundCloud API.
     *
     * @param song
     *            the song to parse the stream URL of
     * @return the URL of the stream
     * @throws SoundCloudException
     *             if the stream could not be parsed
     * @throws IOException
     *             if the mediaURL is malformed or if the JSON page could not be
     *             downloaded.
     */
    protected String parseStreamUrl(final SoundCloudTrack song) throws SoundCloudException,
            IOException {
        final String streamJsonUrl = "http://api.soundcloud.com/i1/tracks/" + song.getId()
                + "/streams?client_id=" + CLIENT_ID + "&secret_token=None";

        final String jsonPage = this.getUrlFactory().getContent(streamJsonUrl);

        final JSONObject root = new JSONObject(jsonPage);

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
     * @param artist
     *            the artist
     * @param title
     *            the title
     * @return the resolved URL
     */
    protected String resolveUrl(final String artist, final String title) {
        String url = null;

        try {
            url = "https://soundcloud.com/" + URLEncoder.encode(artist, "UTF-8") + "/"
                    + URLEncoder.encode(title, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "https://api.soundcloud.com/resolve.json?url=" + url + "&client_id=" + CLIENT_ID;
    }

    /**
     * Parse the information Json of the SoundCloud API.
     *
     * @param infoUrl
     *            the URL of the API request
     * @return the parsed {@link SoundCloudTrack} System.out.println(searchUrl);
     * @throws IOException
     *             if the URL is malformed or could not be downloaded.
     */
    protected SoundCloudTrack parseInfoJson(final String infoUrl) throws IOException {
        final String jsonPage = this.getUrlFactory().getContent(infoUrl);

        final JSONObject root = new JSONObject(jsonPage);

        return this.parseTrack(root);
    }

}
