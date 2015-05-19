package me.moodcat.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
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
    public List<Room> getRooms(@QueryParam("mood") final List<String> moods,
            @QueryParam("limit") int limit) {
        VAVector targetVector = VAVector.createTargetVector(moods);
        List<Room> allRooms = roomDAO.listRooms(targetVector, limit);

        if (limit > allRooms.size()) {
            return allRooms;
        } else if (limit == 1) {
            return allRooms.subList(0, 1);
        } else if (limit > 1) {
            return allRooms.subList(0, limit);
        }

        return allRooms;
    }

    @GET
    @Path("{id}")
    @Transactional
    public Response getRoom(@PathParam("id") final Integer roomId) {
        if (roomId == null) {
            return Response.serverError().entity("id cannot be blank").build();
        }
        return Response.ok(roomDAO.findById(roomId.intValue())).build();
    }

    @GET
    @Path("{id}/messages")
    @Transactional
    public List<ChatMessage> getMessages(@PathParam("id") final int roomId) {
        return roomDAO.listMessages(roomId);
    }

    @POST
    @Path("{id}/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response postChatMessage(@PathParam("id") final int id,
            @FormParam("author") String author, @FormParam("message") String message) {
        Preconditions.checkNotNull(author);
        ChatMessage msg = new ChatMessage();
        msg.setAuthor(author);
        msg.setMessage(message);
        msg.setRoom(roomDAO.findById(id));
        roomDAO.addMessage(msg);

        return Response.ok().build();
    }
}
