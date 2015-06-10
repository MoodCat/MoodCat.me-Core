package me.moodcat.soundcloud.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response for the me request.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeModel {

    /**
     * Soundcloud Id for the user.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * Username for the user.
     */
    @JsonProperty("permalink")
    private String permalink;

    /**
     * Name for the user.
     */
    @JsonProperty("username")
    private String username;

    /**
     * Avatar URL.
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

}
