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
	 * @param entityManager current entity manager
	 */
	@Inject
	public UserDAO(final EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Find a user by its soundcloud id.
	 *
	 * @param soundCloudId Soundcloud id for the user
	 * @return The user entity
	 */
	public User retrieveBySoundcloudId(final Integer soundCloudId) {
		return ensureExists(query().from(user)
			.where(user.soundcloud_id.eq(soundCloudId))
			.singleResult(user));
	}

}
