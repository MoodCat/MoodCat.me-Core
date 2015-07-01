package me.moodcat.backend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.NotAuthorizedException;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.users.User;
import me.moodcat.soundcloud.SoundCloudException;
import me.moodcat.soundcloud.SoundCloudIdentifier;
import me.moodcat.soundcloud.models.MeModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class UserBackendTest {

    private static final String BOGUS = "bogus";

    private static final String TOKEN = "user_token";

    private static final int USER_ID = 2;

    private static final int ANOTHER_USER_ID = 3;

    @Mock
    private Provider<UserDAO> userDAOProvider;

    @Mock
    private SoundCloudIdentifier soundCloudIdentifier;

    @InjectMocks
    private UserBackend userBackend;

    @Mock
    private User user;

    private User anotherUser;

    @Mock
    private MeModel meModel;

    @Mock
    private UserDAO userDAO;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Before
    public void setUp() throws SoundCloudException {
        when(userDAOProvider.get()).thenReturn(userDAO);
        when(soundCloudIdentifier.getMe(TOKEN)).thenReturn(meModel);
        when(soundCloudIdentifier.getMe(BOGUS))
                .thenThrow(new SoundCloudException("Invalid token."));

        when(meModel.getId()).thenReturn(USER_ID);

        when(userDAO.findBySoundcloudId(USER_ID)).thenReturn(user);
        when(userDAO.merge(user)).thenReturn(user);
        when(userDAO.persist(Matchers.any())).thenAnswer(
                (invocation) -> invocation.getArgumentAt(0, User.class));

        anotherUser = new User();
        anotherUser.setAccessToken(TOKEN);
        anotherUser.setSoundCloudUserId(ANOTHER_USER_ID);
    }

    @Test
    public void canRetrieveUser() {
        when(userDAO.findByAccessToken(anyString())).thenThrow(new EntityNotFoundException());
        assertEquals(user, userBackend.loginUsingSoundCloud(TOKEN));
        verify(user).setAccessToken(TOKEN);
    }

    @Test(expected = NotAuthorizedException.class)
    public void whenUserIsNotFoundWithTokenThrowNotAuthorized() {
        when(userDAO.findByAccessToken(anyString())).thenThrow(new EntityNotFoundException());
        userBackend.loginUsingSoundCloud(BOGUS);
    }

    @Test
    public void whenUserIsNotFoundCreateTheUser() {
        when(meModel.getId()).thenReturn(ANOTHER_USER_ID);
        when(userDAO.findByAccessToken(anyString())).thenThrow(new EntityNotFoundException());
        when(userDAO.findBySoundcloudId(ANOTHER_USER_ID)).thenThrow(new EntityNotFoundException());

        assertEquals(anotherUser, userBackend.loginUsingSoundCloud(TOKEN));
    }
    
    @Test
    public void canRetrieveUserByToken() {
        when(userDAO.findByAccessToken(TOKEN)).thenReturn(anotherUser);

        assertEquals(anotherUser, userBackend.loginUsingSoundCloud(TOKEN));
    }

}
