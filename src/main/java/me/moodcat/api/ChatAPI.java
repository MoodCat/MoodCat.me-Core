package me.moodcat.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

@Path("/api/rooms/{roomId}/chat")
@Produces(MediaType.APPLICATION_JSON)
public class ChatAPI {

    private final ChatDAO chatDAO;

    private final RoomDAO roomDAO;

    @Inject
    @VisibleForTesting
    public ChatAPI(final ChatDAO chatDAO, final RoomDAO roomDAO) {
        this.chatDAO = chatDAO;
        this.roomDAO = roomDAO;
    }

    @GET
    public List<ChatMessage> getChatList(@PathParam(value = "roomId") final int roomId) {
        return this.chatDAO.listByRoomId(this.roomDAO.findById(roomId));
    }

    @POST
    @Path("/post")
    public String postMessage(final String message) {
        return "Success";
    }

}
