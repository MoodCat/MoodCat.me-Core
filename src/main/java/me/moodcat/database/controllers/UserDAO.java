package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QUser.user;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import me.moodcat.database.entities.User;

import com.google.inject.persist.Transactional;

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
     * Find a user by its id.
     *
     * @param id
     *            id for the user
     * @return The user entity
     */
    @Transactional
    public User findById(final int id) {
        return ensureExists(query().from(user)
                .where(user.id.eq(id))
                .singleResult(user));
    }

    /**
     * Find a user by its soundcloud id.
     *
     * @param soundCloudId
     *            Soundcloud id for the user
     * @return The user entity
     */
    @Transactional
    public User findBySoundcloudId(final Integer soundCloudId) {
        return ensureExists(query().from(user)
                .where(user.soundCloudUserId.eq(soundCloudId))
                .singleResult(user));
    }

    /**
     * Find a user by its soundcloud token.
     *
     * @param accessToken
     *            Soundcloud token for the user
     * @return The user entity
     */
    @Transactional
    public User findByAccessToken(final String accessToken) {
        return ensureExists(query().from(user)
                .where(user.accessToken.eq(accessToken))
                .singleResult(user));
    }

    /**
     * Retrieve all users.
     * 
     * @return A list of all users.
     */
    @Transactional
    public List<User> getAll() {
        return this.query().from(user).list(user);
    }

    /**
     * Updates the user the set amount.
     * 
     * @param user
     *            The user to update.
     * @param amount
     *            The amount of points to award the user.
     */
    public void incrementPoints(final User user, final int amount) {
        user.increment(amount);
        this.merge(user);
    }

    /**
     * Retrieves a list of {limit} users, sorted on their score.
     * 
     * @param limit
     *            The number of users to retrieve.
     * @return A list of the most awarded users.
     */
    @Transactional
    public List<User> getLeaderboard(final long limit) {
        return this.query()
                .from(user)
                .orderBy(user.points.desc())
                .limit(limit)
                .list(user);
    }

}
