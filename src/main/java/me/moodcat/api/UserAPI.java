package me.moodcat.api;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.moodcat.api.models.UserModel;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The API for the room.
 */
@Path("/api/users/")
@Produces(MediaType.APPLICATION_JSON)
public class UserAPI {

    /**
     * Provider for the current user.
     */
    private final Provider<User> currentUserProvider;

    /**
     * Access to the user DAO.
     */
    private final UserDAO userDAO;

    @Inject
    @VisibleForTesting
    public UserAPI(final UserDAO userDAO,
                   @Named("current.user") final Provider<User> currentUserProvider) {
        this.userDAO = userDAO;
        this.currentUserProvider = currentUserProvider;
    }

    @GET
    @Transactional
    public List<UserModel> getUsers() {
        return Lists.transform(userDAO.getAll(), UserModel::transform);
    }

    /**
     * Get the user according to the provided id.
     *
     * @param userId
     *            The id of the user to find.
     * @return The user according to the id.
     * @throws IllegalArgumentException
     *             If the id is null.
     */
    @GET
    @Path("{id}")
    @Transactional
    public UserModel getUser(@PathParam("id") final int userId) {
        return UserModel.transform(userDAO.retrieveBySoundcloudId(userId));
    }

    /**
     * Returns the current user
     * 
     * @return
     */
    @GET
    @Path("me")
    @Transactional
    public UserModel getMe() {
        return UserModel.transform(currentUserProvider.get());
    }

    /**
     * The amount of points a user with id {id} has.
     * 
     * @param userId
     *            The user id we need the points for.
     * @return The amount of points the user has.
     */
    @GET
    @Path("{id}/points")
    @Transactional
    public Integer getPoints(@PathParam("id") final int userId) {
        return userDAO.retrievePointsBySoundcloudId(userId);
    }

    /**
     * The amount of points a user with id userId has earned.
     * 
     * @param userId
     *            The user to update.
     * @param amount
     *            The amount of points to be awarded.
     */
    @POST
    @Path("{id}/points")
    @Transactional
    public void addPoints(@PathParam("id") final int userId,
            @QueryParam("amount") @DefaultValue("0") final int amount) {
        userDAO.incrementPoints(userId, amount);
    }

    @GET
    @Path("leaderboard")
    @Transactional
    public List<UserModel> getLeaderboard(@QueryParam("limit") @DefaultValue("10") final long limit) {
        return Lists.transform(userDAO.getLeaderboard(limit), UserModel::transform);
    }
}
