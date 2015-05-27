package me.moodcat.soundcloud;

import javax.ws.rs.client.Client;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * Connects to the SoundCloud API using its {@link #createClient()}.
 *
 * @author JeremyBellEU
 */
public abstract class SoundCloudAPIConnector {

    /**
     * The charset that is used to encode urls.
     */
    protected static final String URI_CHARSET = "UTF-8";

    /**
     * The host-name able to be formatted with a sub-domain of SoundCloud.
     */
    protected static final String SOUNDCLOUD_HOST_FORMAT_STRING = "https://%ssoundcloud.com";

    /**
     * api.soundcloud.com takes care of all API calls.
     */
    protected static final String SOUNDCLOUD_API = String.format(SOUNDCLOUD_HOST_FORMAT_STRING,
            "api.");

    /**
     * soundcloud.com is the general host-name.
     */
    protected static final String SOUNDCLOUD_HOST = String.format(SOUNDCLOUD_HOST_FORMAT_STRING,
            "");

    /**
     * Our client-id in order to talk to SoundCloud.
     */
    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    /**
     * Obtain a HTTP-client to start a request.
     *
     * @return The HTTP-client.
     */
    protected Client createClient() {
        return ResteasyClientBuilder.newBuilder().build();
    }

}
