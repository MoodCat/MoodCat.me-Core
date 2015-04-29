package me.moodcat.soundcloud;

import javax.ws.rs.client.Client;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * Connects to the SoundCloud API using its {@link #urlFactory}.
 *
 * @author JeremyBellEU
 */
public abstract class SoundCloudAPIConnector {

    protected static final String SOUNDCLOUD_HOST_FORMAT_STRING = "https://%s.soundcloud.com";

    protected static final String SOUNDCLOUD_API = String.format(SOUNDCLOUD_HOST_FORMAT_STRING,
            "api");

    protected static final String SOUNDCLOUD_HOST = String.format(SOUNDCLOUD_HOST_FORMAT_STRING,
            "www");

    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    protected Client createClient() {
        return ResteasyClientBuilder.newBuilder().build();
    }

}
