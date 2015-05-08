package me.moodcat.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
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
import com.google.inject.Singleton;

/**
 * API to send and check for messages in a room.
 */
@Singleton
@Path("/api/rooms/{roomId}/chat")
@Produces(MediaType.APPLICATION_JSON)
public class ChatAPI {

    /**
     * The chat-manager to talk to the database.
     */
    private final ChatDAO chatDAO;

    /**
     * The room-manager to talk to the database.
     */
    private final RoomDAO roomDAO;

    /**
     * The cached list of messages.
     */
    private List<ChatMessage> messages;

    /**
     * API to send and check for messages in a room.
     *
     * @param chatDAO
     *            The chatMessages
     * @param roomDAO
     *            The rooms
     */
    @Inject
    @VisibleForTesting
    public ChatAPI(final ChatDAO chatDAO, final RoomDAO roomDAO) {
        this.chatDAO = chatDAO;
        this.roomDAO = roomDAO;

        this.messages = new ArrayList<ChatMessage>();
    }

    /**
     * Get all the messages from the provided room.
     *
     * @param roomId
     *            The room to get the messages from.
     * @return The messages for this room.
     */
    @GET
    public List<ChatMessage> getChatList(@PathParam(value = "roomId") final int roomId) {
        return this.messages;

        // return this.chatDAO.listByRoomId(this.roomDAO.findById(roomId));
    }

    /**
     * Post a message to the room chat.
     *
     * @param input
     *            The message to sent with roomId
     * @return Whether the message was succesfully posted or not.
     */
    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public ChatMessagePostResponse postMessage(final ChatMessageRequest input) {
        final ChatMessagePostResponse response = new ChatMessagePostResponse();
        response.setStatusCode("Success");

        final ChatMessage message = new ChatMessage();
        message.setMessage(input.getMessage());
        message.setRoom(this.roomDAO.findById(input.getRoomId()));
        message.setTimestamp(System.currentTimeMillis());
        message.setAuthor(input.getAuthor());

        this.messages.add(message);
        this.chatDAO.persist(message);

        return response;
    }

    /**
     * The input for {@link ChatAPI#postMessage(ChatMessageRequest)}.
     *
     * @author JeremybellEU
     */
    @Data
    static class ChatMessageRequest {

        /**
         * The message to post.
         */
        private String message;

        /**
         * The room the message must be sent to.
         */
        private int roomId;

        /**
         * The author that sent the message.
         */
        private String author;
    }

    /**
     * Response to indicate whether the chat message was succesfully posted or not.
     *
     * @author JeremybellEU
     */
    @Data
    static class ChatMessagePostResponse {

        /**
         * Can be either success or failure.
         */
        private String statusCode;
    }

}
