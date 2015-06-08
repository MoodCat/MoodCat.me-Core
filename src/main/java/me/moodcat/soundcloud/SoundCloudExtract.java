package me.moodcat.soundcloud;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * An extractor that can connect to SoundCloud using a {@link HttpClientInvoker}.
 */
public class SoundCloudExtract extends SoundCloudAPIConnector {

    public static void main(String[] args) throws SoundCloudException {
        SoundCloudTrack track = new SoundCloudExtract().extract(203831026);
        System.out.println(track.getPurchaseUrl());
        System.out.println(track.getPurchaseTitle());
    }

    /**
     * The pattern of an API call to get data about a track.
     */
    protected static final Pattern SONG_PATTERN = Pattern
            .compile(SOUNDCLOUD_HOST + "/(?<artist>.*?)/(?<permalink>.*?)$");

    /**
     * {@link HttpClientInvoker} used to connect to the internet.
     *
     * @param urlFactory
     *            The new factory to connect to the internet.
     * @return The factory that is used to connect to the internet.
     */
    @Setter
    @Getter
    private HttpClientInvoker urlFactory;

    /**
     * Create an extractor.
     */
    public SoundCloudExtract() {
        this.urlFactory = new HttpClientInvoker();
    }

    @SneakyThrows
    protected static String getUrlFromArtistAndPermalink(final String artist,
            final String permalink) {
        return String.format(SOUNDCLOUD_HOST + "/%s/%s",
                URLEncoder.encode(artist, URI_CHARSET),
                URLEncoder.encode(permalink, URI_CHARSET));
    }

    /**
     * Retrieve a SoundCloudTrack given a SoundCloud URL.
     *
     * @param soundCloudUrl
     *            the given SoundCloud URL
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException
     *             if the URL is malformed
     */
    public SoundCloudTrack extract(final String soundCloudUrl) throws SoundCloudException {
        final Matcher matcher = SONG_PATTERN.matcher(soundCloudUrl);

        if (matcher.find()) {
            final String permalink = matcher.group("permalink");
            final String artist = matcher.group("artist");
            return this.extract(artist, permalink);
        }

        throw new SoundCloudException("Wrong URL supplied");
    }

    /**
     * Retrieve a SoundCloudTrack given a SoundCloud id.
     *
     * @param id
     *            the given SoundCloud id
     * @return the parsed {@link SoundCloudTrack}
     * @throws SoundCloudException
     *             if the URL is malformed
     */
    public SoundCloudTrack extract(final int id) throws SoundCloudException {
        return this.getUrlFactory().retrieve(id, SoundCloudTrack.class);
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
        return this.getUrlFactory().resolve(url, SoundCloudTrack.class);
    }

    /**
     * Mockable HttpClientInvoker that takes care of network connection.
     */
    protected class HttpClientInvoker {

        /**
         * The resolve resource allows you to lookup and access API resources
         * when you only know the SoundCloud.com URL.
         *
         * @param url
         *            The url to retrieve.
         * @param targetEntity
         *            What entity type should be returned.
         * @param <T>
         *            The type of the entity.
         * @return The entity that corresponds to the request.
         * @throws SoundCloudException
         *             Thrown if the resource could not be accessed.
         */
        protected <T> T resolve(final String url, final Class<T> targetEntity)
                throws SoundCloudException {
            return perform(url, target -> target
                    .request()
                    .get(targetEntity));
        }

        /**
         * The retrieve resource allows you to retrieve access API resources given a SoundCloud id,
         * this method avoids the need of resolving the the URL through an extra request.
         *
         * @param id
         *            the url to retrieve
         * @param <T>
         *            The type of the entity.
         * @param targetEntity
         *            The entity to retrieve from the Soundcloud API.
         * @return
         *         The object of type T from the API
         * @throws SoundCloudException
         *             if the resource could not be accessed.
         */
        protected <T> T retrieve(final int id, final Class<T> targetEntity)
                throws SoundCloudException {
            return perform(target -> target.path("tracks")
                    .path(id + ".json")
                    .queryParam("client_id", CLIENT_ID)
                    .request()
                    .get(targetEntity));
        }

    }

}
