package me.moodcat.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class UserAPITest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private Provider<User> currentUserProvider;

    @InjectMocks
    private UserAPI userAPI;

    private List<User> users;

    @Mock
    private User oneUser;

    @Mock
    private User anotherUser;

    @Mock
    private User me;

    @Before
    public void setUp() {
        users = Lists.newArrayList();
        users.add(oneUser);
        users.add(anotherUser);

        when(oneUser.getId()).thenReturn(1);

        when(userDAO.getAll()).thenReturn(users);
        when(userDAO.findById(1)).thenReturn(oneUser);
        when(userDAO.getLeaderboard(Matchers.anyLong())).thenReturn(users);

        when(currentUserProvider.get()).thenReturn(me);
        when(me.getId()).thenReturn(4);
        when(me.getPoints()).thenReturn(10);
    }

    @Test
    public void getAllUsers() {
        assertEquals(2, userAPI.getUsers().size());
    }

    @Test
    public void getAUser() {
        assertEquals(1, userAPI.getUser(1).getId().intValue());
    }

    @Test
    public void getMe() {
        assertEquals(4, userAPI.getMe().getId().intValue());
    }

    @Test
    public void getPoints() {
        assertEquals(10, userAPI.getPoints().intValue());
    }

    @Test
    public void getLeaderboard() {
        assertEquals(1, userAPI.getLeaderboard(25).get(0).getId().intValue());
    }
}
