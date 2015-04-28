package me.moodcat.soundcloud;

import java.io.IOException;

public class SoundCloudTrack {

    /**
     * The track's title.
     */
    String title;

    /**
     * The track's permalink.
     */
    String permalink;

    /**
     * The track's username.
     */
    String username;

    /**
     * The track's artwork URL.
     */
    String artworkUrl;

    /**
     * The track's stream URL.
     */
    String streamUrl;

    /**
     * The track's duration in ms.
     */
    int duration;

    /**
     * The track's id.
     */
    int id;

    /**
     * Whether the track is downloadable.
     */
    boolean downloadable;

    /**
     * Create a SoundCloudTrack object.
     *
     * @param id           the unique SoundCloud track id
     * @param title        the title of the track
     * @param permalink    the permalink of the track
     * @param username     the username of the track's uploader
     * @param artworkUrl   the url to the tracks' artwork
     * @param duration     the duration of the track in milliseconds
     * @param downloadable boolean indicating whether the track is downloadable
     */
    public SoundCloudTrack(int id, String title, String permalink, String username,
                           String artworkUrl, int duration, boolean downloadable) {
        super();
        this.id = id;
        this.title = title;
        this.permalink = permalink;
        this.username = username;
        this.artworkUrl = artworkUrl;
        this.duration = duration;
        this.downloadable = downloadable;
    }

    public boolean hasStreamUrl() {
        return streamUrl != null;
    }

    /**
     * Return or generate and return the streamUrl.
     *
     * @return the URL of the stream
     */
    public String getStreamUrl() {
        return streamUrl;
    }

    /**
     * Generate the stream URL.
     *
     * @throws IOException         when a download has failed
     * @throws SoundCloudException when a stream URL could not be generated
     */
    public void generateStreamUrl() throws IOException, SoundCloudException {
        if (streamUrl != null) {
            return;
        }
        if (downloadable) {
            streamUrl = "https://api.soundcloud.com/tracks/" + id + "/download?client_id=" + SoundCloudExtract.CLIENT_ID;
        } else {
            streamUrl = new SoundCloudExtract().parseStreamUrl(this);
        }
    }

    /**
     * Get the track's artwork URL.
     *
     * @return the artwork URL
     */
    public String getArtworkUrl() {
        return artworkUrl;
    }

    /**
     * Get whether the track is downlodable.
     *
     * @return the downloadable boolean
     */
    public boolean isDownloadable() {
        return downloadable;
    }

    /**
     * Get the track's id.
     *
     * @return the trakck's id
     */
    public int getId() {
        return id;
    }

    /**
     * Return the title of the track.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "SoundCloudTrack{"
                + "title='" + title + '\''
                + ", permalink='" + permalink + '\''
                + ", username='" + username + '\''
                + ", artworkUrl='" + artworkUrl + '\''
                + ", streamUrl='" + streamUrl + '\''
                + ", duration=" + duration
                + ", id=" + id
                + ", downloadable=" + downloadable
                + '}';
    }
}
