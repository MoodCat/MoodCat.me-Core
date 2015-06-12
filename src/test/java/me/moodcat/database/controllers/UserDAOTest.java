package me.moodcat.database.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.bootstrapper.BootstrapRule;
import me.moodcat.database.bootstrapper.TestBootstrap;
import me.moodcat.database.entities.User;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * This methods test to persist a User
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class UserDAOTest {

    private final static String USER_NAME = "someName";

    private final static Integer SOUNDCLOUD_ID = 12165;

    /**
     * The UserDAO.
     */
    @Rule
    @Inject
    public BootstrapRule bootstrapRule;

    // Public for JUnit, it's required. Not unused either :)

    /**
     * The UserDAO.
     */
    @Inject
    private UserDAO userDAO;

    /**
     * Persist a user with data.
     */
    @Test
    public void persistUser() {
        final User user = new User();
        user.setName(USER_NAME);
        user.setSoundCloudUserId(SOUNDCLOUD_ID);
        user.setPoints(1);
        userDAO.persist(user);

        final User actual = userDAO.findBySoundcloudId(SOUNDCLOUD_ID);
        assertEquals(user, actual);
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void canRetrieveBySoundcloudId() {
        assertEquals("Gijs", userDAO.findBySoundcloudId(1).getName());
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void cangetLeaderboardStandardFilter() {
        final int standardFilter = 10;
        final int expectedNumberOfUsers = 5;

        assertEquals(expectedNumberOfUsers, userDAO.getLeaderboard(standardFilter).size());
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void cangetLeaderboardSmallFilter() {
        final int filter = 2;
        final int expectedNumberOfUsers = 2;

        assertEquals(expectedNumberOfUsers, userDAO.getLeaderboard(filter).size());
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void testOrder() {
        final int filter = 2;
        ArrayList<User> bestUsers = (ArrayList<User>) userDAO.getLeaderboard(filter);

        assertTrue(bestUsers.get(0).getPoints() > bestUsers.get(1).getPoints());
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void canIncrementPoints() {
        final int incrementAmount = 6;
        final int soundCloudId = 1;

        int oldscore = userDAO.findBySoundcloudId(soundCloudId).getPoints();
        userDAO.incrementPoints(soundCloudId, incrementAmount);
        assertEquals((oldscore + incrementAmount), userDAO.findBySoundcloudId(soundCloudId)
                .getPoints());
    }

    @Test
    @TestBootstrap("/bootstrap/users.json")
    public void canGetAll() {
        final int expectedNumberOfUsers = 5;

        assertEquals(expectedNumberOfUsers, userDAO.getAll().size());
    }

}
