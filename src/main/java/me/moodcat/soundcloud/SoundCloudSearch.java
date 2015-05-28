package me.moodcat.soundcloud;

import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.client.Client;

import lombok.Data;
import lombok.SneakyThrows;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request SoundCloud to supply tracks for a search-request.
 */
public class SoundCloudSearch extends SoundCloudAPIConnector {

    /**
     * Search SoundCloud for tracks.
     *
     * @param query
     *            the query to search for
     * @return the search results
     * @throws SoundCloudException
     *             if the API response could not be downloaded
     */
    public List<SoundCloudTrack> search(final String query) throws SoundCloudException {
        final Client client = ResteasyClientBuilder.newBuilder().build();

        try {
            return client.target(SOUNDCLOUD_API)
                    .path("search")
                    .queryParam("client_id", CLIENT_ID)
                    .queryParam("q", encode(query))
                    .request()
                    .get(SearchResponse.class)
                    .getTracks();
        } catch (final Exception e) {
            throw new SoundCloudException(e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    @SneakyThrows
    protected static String encode(final String url) {
        return URLEncoder.encode(url, URI_CHARSET);
    }

    /**
     * The response of a search request with a list of tracks.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResponse {

        /**
         * A list of tracks that were supplied by SoundCloud for the search request.
         *
         * @param tracks
         *            The list of tracks to set.
         * @return The list of tracks that were supplied by SoundCloud.
         */
        @JsonProperty("collection")
        private List<SoundCloudTrack> tracks;
    }

}
