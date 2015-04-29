package me.moodcat.soundcloud;

import lombok.Getter;
import lombok.Setter;
import me.moodcat.utils.network.UrlStreamFactory;

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

}
