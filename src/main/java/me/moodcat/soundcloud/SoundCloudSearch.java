package me.moodcat.soundcloud;

import java.net.URLEncoder;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.SneakyThrows;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.ws.rs.client.Client;

public class SoundCloudSearch {

    protected final static String SOUNDCLOUD_HOST = "http://api.soundcloud.com";
    protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

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
        Client client = ResteasyClientBuilder.newBuilder().build();

        try {
            return client.target(SOUNDCLOUD_HOST)
                    .path("search")
                    .queryParam("client_id", CLIENT_ID)
                    .queryParam("q", encode(query))
                    .request()
                    .get(SearchResponse.class)
                    .getTracks();
        }
        catch (Exception e) {
            throw new SoundCloudException(e.getMessage(), e);
        }
        finally {
            client.close();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResponse {

        @JsonProperty("collection")
        private List<SoundCloudTrack> tracks;

    }

    @SneakyThrows
    protected static String encode(final String url) {
        return URLEncoder.encode(url, "UTF-8");
    }

}
