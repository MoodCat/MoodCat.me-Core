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
     * Access to the user DAO.
     */
    private final UserDAO userDAO;

    @Inject
    @VisibleForTesting
    public UserAPI(final UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GET
    @Transactional
    public List<User> getUsers() {
        return userDAO.getAll();
    }

    /**
     * Get the user according to the provided id.
     *
     * @param id
     *            The id of the user to find.
     * @return The user according to the id.
     * @throws IllegalArgumentException
     *             If the id is null.
     */
    @GET
    @Path("{id}")
    @Transactional
    public User getUser(@PathParam("id") final int userId) {
        return userDAO.retrieveBySoundcloudId(userId);
    }

    /**
     * Returns the current user
     * 
     * @return
     */
    @GET
    @Path("me")
    @Transactional
    public User getMe() {
        // FIXME: Hardcoded until Oauth works
        return userDAO.retrieveBySoundcloudId(1);
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
}
