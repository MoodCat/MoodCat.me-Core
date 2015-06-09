package me.moodcat.soundcloud;

import java.util.List;

import lombok.Data;

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
        return perform(target -> target.path("search")
            .queryParam("client_id", CLIENT_ID)
            .queryParam("q", encode(query))
            .request()
            .get(SearchResponse.class)
            .getTracks());
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
