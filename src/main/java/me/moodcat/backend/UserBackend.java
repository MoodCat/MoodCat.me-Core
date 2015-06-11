package me.moodcat.backend;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.NotAuthorizedException;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;
import me.moodcat.soundcloud.SoundCloudException;
import me.moodcat.soundcloud.SoundCloudIdentifier;
import me.moodcat.soundcloud.models.MeModel;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

/**
 * The UserBackend allows users to login through SoundCloud.
 */
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
        MeModel me = retrieveMe(token);
        return findOrRegisterUser(me, token);
    }

    private MeModel retrieveMe(final String token) {
        try {
            return soundCloudIdentifier.getMe(token);
        } catch (SoundCloudException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    @Transactional
    private User findOrRegisterUser(final MeModel me, final String token) {
        final UserDAO userDAO = this.userDAOProvider.get();
        final int soundCloudId = me.getId();

        try {
            final User user = userDAO.retrieveBySoundcloudId(soundCloudId);
            user.setAccessToken(token);
            return userDAO.merge(user);
        } catch (EntityNotFoundException e) {
            User user = createUser(soundCloudId, me);
            user.setAccessToken(token);
            return userDAO.persist(user);
        }
    }

    private static User createUser(final Integer soundCloudId, final MeModel me) {
        final User user = new User();
        user.setSoundCloudUserId(soundCloudId);
        user.setAvatarUrl(me.getAvatarUrl());
        user.setName(me.getFullName());
        return user;
    }

}
