package me.moodcat.database.controllers;

import static org.junit.Assert.assertEquals;
import me.moodcat.database.DatabaseTestModule;
import me.moodcat.database.bootstrapper.BootstrapRule;
import me.moodcat.database.bootstrapper.TestBootstrap;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * This methods test to persist a Song
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(DatabaseTestModule.class)
public class RoomDAOTest {

    /**
     * The ArtistDAO.
     */
    @Rule
    @Inject
    public BootstrapRule bootstrapRule;
    // Public for JUnit, it's required. Not unused either :)

    /**
     * The ArtistDAO.
     */
    @Inject
    private RoomDAO roomDAO;

    @Test
    @TestBootstrap("/bootstrap/rooms.json")
    public void canRetrieveAllRooms() {
        assertEquals(3, roomDAO.listRooms().size());
    }

    @Test
    @TestBootstrap("/bootstrap/rooms.json")
    public void canRetrieveAllRoomsWithLimit() {
        assertEquals(1, roomDAO.listRooms(1).size());
    }

    @Test
    @TestBootstrap("/bootstrap/rooms.json")
    public void canRetrieveById() {
        assertEquals(2, roomDAO.findById(2).getId().intValue());
    }

    @Test
    @TestBootstrap("/bootstrap/rooms.json")
    public void canRetrieveChatMessages() {
        assertEquals("Welcome to Moodcat!", roomDAO.listMessages(1).get(0).getMessage());
    }

}
