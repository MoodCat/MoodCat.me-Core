package me.moodcat.soundcloud;

import lombok.Getter;
import lombok.Setter;
import me.moodcat.utils.network.UrlStreamFactory;

import org.json.JSONObject;

/**
 * Connects to the SoundCloud API using its {@link #urlFactory}.
 * 
 * @author Tim Laptop
 */
public abstract class SoundCloudAPIConnector {

    @Setter
    @Getter
    private UrlStreamFactory urlFactory;

    public SoundCloudAPIConnector() {
        this.urlFactory = new UrlStreamFactory();
    }

    /**
     * Parse a single track's JSON.
     *
     * @param element
     *            the JSONObject to parse
     * @return the parsed track
     */
    protected SoundCloudTrack parseTrack(final JSONObject element) {
        final int id = element.getInt("id");
        final String title = element.getString("title");
        final String permalink = element.getString("permalink");
        final String username = element.getJSONObject("user").getString("username");
        final String artworkUrl = element.isNull("artwork_url") ? null : element
                .getString("artwork_url");
        final int duration = element.getInt("duration");
        final boolean downloadable = element.getBoolean("downloadable");

        return new SoundCloudTrack(id, title, permalink, username, artworkUrl, duration,
                downloadable);
    }

}
