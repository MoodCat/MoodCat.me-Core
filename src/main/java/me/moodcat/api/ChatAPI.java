package me.moodcat.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.Data;
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

    /**
     * Post a message to the room chat.
     * 
     * @param message
     *            The message to sent
     * @return Whether the message was succesfully posted or not.
     */
    @POST
    @Path("/post")
    public ChatMessagePostResponse postMessage(final String message) {
        final ChatMessagePostResponse response = new ChatMessagePostResponse();
        response.setStatusCode("Success");

        return response;
    }

    /**
     * Response to indicate whether the chat message was succesfully posted or not.
     *
     * @author JeremybellEU
     */
    @Data
    class ChatMessagePostResponse {

        /**
         * Can be either success or failure.
         */
        private String statusCode;
    }

}
