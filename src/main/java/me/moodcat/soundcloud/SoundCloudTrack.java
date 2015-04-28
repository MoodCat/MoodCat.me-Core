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
    public SoundCloudTrack(final int id, final String title, final String permalink,
            final String username, final String artworkUrl, final int duration,
            final boolean downloadable) {
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
        return this.streamUrl != null;
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
        if (this.streamUrl != null) {
            return;
        }
        if (this.downloadable) {
            this.streamUrl = "https://api.soundcloud.com/tracks/" + this.id
                    + "/download?client_id=" + SoundCloudExtract.CLIENT_ID;
        } else {
            this.streamUrl = new SoundCloudExtract().parseStreamUrl(this);
        }
    }
}
