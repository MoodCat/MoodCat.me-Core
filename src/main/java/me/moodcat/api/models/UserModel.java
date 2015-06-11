package me.moodcat.api.models;

import lombok.Data;
import me.moodcat.database.entities.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User model for the PAI.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel {

    /**
     * The unique id of the user.
     *
     * @param id
     *            The new Id to set.
     * @return The id of the user.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * The unique id of the user.
     *
     * @param id
     *            The new Id to set.
     * @return The id of the user.
     */
    @JsonProperty("soundCloudUserId")
    private Integer soundCloudUserId;

    /**
     * The name of this user.
     *
     * @param name
     *            The name to set.
     * @return The name of this user.
     */
    @JsonProperty("name")
    private String name;

    /**
     * The avatar of this user.
     *
     * @param avatarUrl
     *            The avatar to set.
     * @return The avatar of this user.
     */
    @JsonProperty("avatarUrl")
    private String avatarUrl;

    /**
     * The amount of points the user has collected.
     * 
     * @return The amount of points the user has
     */
    @JsonProperty("points")
    private int points;

    /**
     * Transform a {@code User} into a {@code UserModel}.
     * 
     * @param user
     *            User to be transformed.
     * @return The transformed user.
     */
    public static UserModel transform(final User user) {
        final UserModel userModel = new UserModel();
        userModel.setName(user.getName());
        userModel.setSoundCloudUserId(user.getSoundCloudUserId());
        userModel.setId(user.getId());
        userModel.setAvatarUrl(user.getAvatarUrl());
        userModel.setPoints(user.getPoints());
        return userModel;
    }

}
