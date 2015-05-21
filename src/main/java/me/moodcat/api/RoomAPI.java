package me.moodcat.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

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

    /**
     * Access to the chat DAO.
     */
    private final ChatDAO chatDAO;

    @Inject
    @VisibleForTesting
    public RoomAPI(final RoomDAO roomDAO, final ChatDAO chatDAO) {
        this.roomDAO = roomDAO;
        this.chatDAO = chatDAO;
    }

    @GET
    @Transactional
    public List<Room> getRooms(@QueryParam("mood") final List<String> moods,
            @QueryParam("limit") @DefaultValue("100000") int limit) {
        VAVector targetVector = VAVector.createTargetVector(moods);
        List<Room> allRooms = roomDAO.listRooms(targetVector, limit);

        if (limit > allRooms.size() || limit < 1) {
            return allRooms;
        } else {
            return allRooms.subList(0, limit);
        }
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
    public Response postChatMessage(ChatMessage msg, @PathParam("id") final int id) {
        System.out.println(msg.toString());
        msg.setRoom(roomDAO.findById(id));
        msg.setTimestamp(System.currentTimeMillis() / 1000);
        chatDAO.addMessage(msg);
        return Response.ok().build();
    }
}
