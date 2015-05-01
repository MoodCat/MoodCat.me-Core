package me.moodcat.soundcloud;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The JSON-representation of the metadata of a SoundCloud track.
 *
 * @author Jaapp--
 */
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
     * The tracks user.
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

    /**
     * A SoundCloud user model.
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

}
