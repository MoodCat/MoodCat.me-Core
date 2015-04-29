package me.moodcat.soundcloud;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import lombok.SneakyThrows;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class SoundCloudExtract extends SoundCloudAPIConnector {

    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    protected static final Pattern songPattern = Pattern
            .compile("https?://(www\\.)?soundcloud.com/(?<artist>.*?)/(?<permalink>.*?)$");

    protected static final String SOUNDCLOUD_HOST = "http://api.soundcloud.com";

    /**
     * Retrieve a SoundCloudTrack given a SoundCloud URL.
     *
     * @param soundCloudUrl
     *            the give SoundCloud URL
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException
     *             if the URL is malformed
     */
    public SoundCloudTrack extract(final String soundCloudUrl) throws SoundCloudException {
        final Matcher matcher = songPattern.matcher(soundCloudUrl);

        if (matcher.find()) {
            final String permalink = matcher.group("permalink");
            final String artist = matcher.group("artist");
            return this.extract(artist, permalink);
        }

        throw new SoundCloudException("Wrong URL supplied");
    }

    /**
     * Retrieve a SoundCloudTrack given the artist and permalink.
     *
     * @param artist
     *            the track's artist
     * @param permalink
     *            the track's permalink
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException
     *             when the download has failed
     * @throws SoundCloudException
     *             When a malformed title has been supplied.
     */
    public SoundCloudTrack extract(final String artist, final String permalink)
            throws SoundCloudException {
        final String url = getUrlFromArtistAndPermalink(artist, permalink);
        return this.resolve(url, SoundCloudTrack.class);
    }

    /**
     * The resolve resource allows you to lookup and access API resources
     * when you only know the SoundCloud.com URL.
     *
     * @param url
     *            the url to retrieve
     * @throws SoundCloudException
     *             if the resource could not be accessed
     */
    protected <T> T resolve(final String url, final Class<T> targetEntity)
            throws SoundCloudException {
        final Client client = ResteasyClientBuilder.newBuilder().build();

        try {
            return client.target(this.redirectLocation(url))
                    .request()
                    .get(targetEntity);
        } catch (final Exception e) {
            throw new SoundCloudException(e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    protected String redirectLocation(final String url) throws SoundCloudException {
        final Client client = ResteasyClientBuilder.newBuilder().build();

        try {
            final Response redirect = client.target(SOUNDCLOUD_HOST)
                    .path("resolve.json")
                    .queryParam("client_id", CLIENT_ID)
                    .queryParam("url", url)
                    .request().get();

            return redirect.getHeaderString("Location");
        } catch (final Exception e) {
            throw new SoundCloudException(e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    @SneakyThrows
    protected static String getUrlFromArtistAndPermalink(final String artist,
            final String permalink) {
        return String.format("https://soundcloud.com/%s/%s",
                URLEncoder.encode(artist, "UTF-8"),
                URLEncoder.encode(permalink, "UTF-8"));
    }

}
