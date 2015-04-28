package me.moodcat.soundcloud;

import java.io.IOException;

import lombok.Data;

@Data
public class SoundCloudTrack {

    /**
     * The track's title.
     */
    private String title;

    /**
     * The track's permalink.
     */
    private String permalink;

    /**
     * The track's username.
     */
    private String username;

    /**
     * The track's artwork URL.
     */
    private String artworkUrl;

    /**
     * The track's stream URL.
     */
    private String streamUrl;

    /**
     * The track's duration in ms.
     */
    private int duration;

    /**
     * The track's id.
     */
    private int id;

    /**
     * Whether the track is downloadable.
     */
    private boolean downloadable;

    /**
     * Create a SoundCloudTrack object.
     *
     * @param id
     *            the unique SoundCloud track id
     * @param title
     *            the title of the track
     * @param permalink
     *            the permalink of the track
     * @param username
     *            the username of the track's uploader
     * @param artworkUrl
     *            the url to the tracks' artwork
     * @param duration
     *            the duration of the track in milliseconds
     * @param downloadable
     *            boolean indicating whether the track is downloadable
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

    /**
     * Whether this track has a stream url.
     * 
     * @return If this track has a stream url.
     */
    public boolean hasStreamUrl() {
        return streamUrl != null;
    }

    /**
     * Generate the stream URL.
     *
     * @throws IOException
     *             when a download has failed
     * @throws SoundCloudException
     *             when a stream URL could not be generated
     */
    public void generateStreamUrl() throws IOException, SoundCloudException {
        if (streamUrl != null) {
            return;
        }
        if (downloadable) {
            streamUrl = "https://api.soundcloud.com/tracks/" + id + "/download?client_id="
                    + SoundCloudExtract.CLIENT_ID;
        } else {
            streamUrl = new SoundCloudExtract().parseStreamUrl(this);
        }
    }
}
