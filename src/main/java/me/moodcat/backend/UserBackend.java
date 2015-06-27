package me.moodcat.backend;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.NotAuthorizedException;

import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;
import me.moodcat.soundcloud.SoundCloudException;
import me.moodcat.soundcloud.SoundCloudIdentifier;
import me.moodcat.soundcloud.models.MeModel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

/**
 * The UserBackend allows users to login through SoundCloud.
 */
@Slf4j
public class UserBackend {

    private final Provider<UserDAO> userDAOProvider;

    private final SoundCloudIdentifier soundCloudIdentifier;

    @Inject
    public UserBackend(final Provider<UserDAO> userDAOProvider,
            final SoundCloudIdentifier soundCloudIdentifier) {
        this.userDAOProvider = userDAOProvider;
        this.soundCloudIdentifier = soundCloudIdentifier;
    }

    /**
     * Login a user using SoundCloud.
     *
     * @param token
     *            The SoundCloud token
     * @return
     *         The User for the token
     */
    public User loginUsingSoundCloud(final String token) {
        return findOrRegisterUser(token);
    }

    private MeModel retrieveMe(final String token) {
        try {
            return soundCloudIdentifier.getMe(token);
        } catch (SoundCloudException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    @Transactional
    private User findOrRegisterUser(final String token) {
        Preconditions.checkNotNull(token);

        final UserDAO userDAO = this.userDAOProvider.get();

        try {
            // Look for user in the caches
            return userDAO.findByAccessToken(token);
        } catch (EntityNotFoundException e) {
            // User not found in caches
            log.debug("User token {} not found in caches, querying SoundCloud", token);
        }

        final MeModel me = retrieveMe(token);
        final int soundCloudId = me.getId();

        try {
            final User user = userDAO.findBySoundcloudId(soundCloudId);

            mergePreviousToken(token, userDAO, user);

            return user;
        } catch (EntityNotFoundException e) {
            User user = createUser(soundCloudId, me);
            user.setAccessToken(token);
            return userDAO.persist(user);
        }
    }

    private void mergePreviousToken(final String token, final UserDAO userDAO, final User user) {
        final String previousToken = user.getAccessToken();

        if (!token.equals(previousToken)) {
            user.setAccessToken(token);
            userDAO.merge(user);
        }
    }

    private static User createUser(final Integer soundCloudId, final MeModel me) {
        final User user = new User();
        user.setSoundCloudUserId(soundCloudId);
        user.setAvatarUrl(me.getAvatarUrl());
        user.setName(me.getUsername());
        return user;
    }

}
