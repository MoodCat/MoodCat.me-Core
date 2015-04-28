package me.moodcat.soundcloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoundCloudTrack {

    /**
     * The track's id.
     */
    @JsonProperty("id")
    private int id;

    /**
     * The track's title.
     */
    @JsonProperty("title")
    private String title;

    /**
     * The track's permalink.
     */
    @JsonProperty("permalink")
    private String permalink;

    /**
     * A SoundCloud user model
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        /**
         * The track's username.
         */
        @JsonProperty("username")
        private String username;

    }

    /**
     * The tracks user
     */
    @JsonProperty("user")
    private User user;

    /**
     * The track's artwork URL.
     */
    @JsonProperty("artwork_url")
    private String artworkUrl;

    /**
     * The track's duration in ms.
     */
    @JsonProperty("duration")
    private int duration;

    /**
     * Whether the track is downloadable.
     */
    @JsonProperty("downloadable")
    private boolean downloadable;

}
