package me.moodcat.api;

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
import me.moodcat.database.entities.Room;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * API to send and check for messages in a room.
 */
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
    }

    /**
     * Get all the messages from the provided room.
     *
     * @param roomId
     *            The room to get the messages from.
     * @return The messages for this room.
     */
    @GET
    @Transactional
    public List<ChatMessage> getChatList(@PathParam(value = "roomId") final int roomId) {
        return this.roomDAO.findById(roomId).getChatMessages();
    }

    /**
     * Post a message to the room chat.
     *
     * @param input
     *            The message to sent with roomId
     * @return Whether the message was successfully posted or not.
     */
    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public ChatMessage postMessage(@PathParam(value = "roomId") final int roomId,
            final ChatMessageRequest input) {
        final Room room = this.roomDAO.findById(roomId);
        final ChatMessage message = new ChatMessage();

        message.setMessage(input.getMessage());
        message.setRoom(room);
        message.setTimestamp(System.currentTimeMillis());
        message.setAuthor(input.getAuthor());

        this.chatDAO.persist(message);
        return message;
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

}
