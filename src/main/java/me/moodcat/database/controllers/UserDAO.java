package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QUser.user;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.mysema.query.types.OrderSpecifier;

import me.moodcat.database.entities.User;

/**
 * Data access object for user entities.
 */
public class UserDAO extends AbstractDAO<User> {

    /**
     * Construct a new user data access object.
     *
     * @param entityManager
     *            current entity manager
     */
    @Inject
    public UserDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Find a user by its soundcloud id.
     *
     * @param soundCloudId
     *            Soundcloud id for the user
     * @return The user entity
     */
    public User retrieveBySoundcloudId(final Integer soundCloudId) {
        return ensureExists(query().from(user)
                .where(user.soundCloudUserId.eq(soundCloudId))
                .singleResult(user));
    }

    /**
     * Returns the amount of points the user has collected.
     * 
     * @param soundCloudId
     *            The Id of the user we're searching for.
     * @return The amount of points this user has.
     */
    public Integer retrievePointsBySoundcloudId(final Integer soundCloudId) {
        return ensureExists(query().from(user)
                .where(user.soundCloudUserId.eq(soundCloudId))
                .singleResult(user)).getPoints();
    }

    /**
     * Retrieve all users.
     * 
     * @return A list of all users.
     */
    public List<User> getAll() {
        return this.query().from(user).list(user);
    }

    /**
     * Updates the user with the id userId with the set amount.
     * 
     * @param soundCloudId
     *            The user to update.
     * @param amount
     *            The amount of points to award the user.
     */
    public void incrementPoints(int soundCloudId, int amount) {
        User usr = this.retrieveBySoundcloudId(soundCloudId);
        usr.increment(amount);
        this.merge(usr);
    }

    /**
     * Retrieves a list of {limit} users, sorted on their score.
     * 
     * @param limit
     *            The number of users to retrieve.
     * @return A list of the most awarded users.
     */
    public List<User> getLeaderboard(final long limit) {
        return this.query()
                .from(user)
                .orderBy(user.points.desc())
                .limit(limit)
                .list(user);
    }

}
