package me.moodcat.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.Room;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The API for the room.
 *
 * @author Jaap Heijligers
 */
@Path("/api/rooms/")
@Produces(MediaType.APPLICATION_JSON)
public class RoomAPI {

    /**
     * The DAO of the room.
     */
    private final RoomDAO roomDAO;

    @Inject
    @VisibleForTesting
    public RoomAPI(final RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    @GET
    @Transactional
    public List<Room> getRooms() {
        return roomDAO.listRooms();
    }

    @GET
    @Path("{id}")
    @Transactional
    public Room getRoom(@PathParam("id") final int roomId) {
        return roomDAO.findById(roomId);
    }

}
