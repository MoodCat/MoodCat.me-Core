package me.moodcat.soundcloud;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The JSON-representation of the metadata of a SoundCloud track.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoundCloudTrack {

    /**
     * The track's id.
     *
     * @param id
     *            The Id of this SoundCloudTrack to set.
     * @return The Id of this SoundCloudTrack.
     */
    @JsonProperty("id")
    private int id;

    /**
     * The track's title.
     *
     * @param title
     *            The title of this track to set.
     * @return The title of this track.
     */
    @JsonProperty("title")
    private String title;

    /**
     * The track's permalink.
     *
     * @param permalink
     *            The permalink to the mp3 of this track to set.
     * @return The permalink to the pm3 of this track.
     */
    @JsonProperty("permalink")
    private String permalink;

    /**
     * The tracks user.
     *
     * @param user
     *            The SoundCloud user that uploaded this song to set.
     * @return The SoundCloud user that uploaded this song.
     */
    @JsonProperty("user")
    private User user;

    /**
     * The track's artwork URL.
     *
     * @param artworkUrl
     *            The url to the artwork image to set.
     * @return The url to the artwork image.
     */
    @JsonProperty("artwork_url")
    private String artworkUrl;

    /**
     * The track's duration in ms.
     *
     * @param duration
     *            The duration of this track to set.
     * @return The duration of this track.
     */
    @JsonProperty("duration")
    private int duration;

    /**
     * Whether the track is downloadable.
     *
     * @param downloadable
     *            If this track is downloadable.
     * @return Whether the track is downloadable or not.
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
         *
         * @param username
         *            The name of this user to set.
         * @return The username of this user.
         */
        @JsonProperty("username")
        private String username;

    }

}
