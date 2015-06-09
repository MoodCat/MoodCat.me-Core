package me.moodcat.database.controllers;

import me.moodcat.database.entities.User;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static me.moodcat.database.entities.QUser.user;

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
    public User findBySoundcloudId(final Integer soundCloudId) {
        return ensureExists(query().from(user)
                .where(user.soundCloudUserId.eq(soundCloudId))
                .singleResult(user));
    }

}
