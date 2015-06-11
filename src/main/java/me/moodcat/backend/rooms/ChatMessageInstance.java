package me.moodcat.backend.rooms;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.util.Transformable;

/**
 * ChatMessageModel.
 */
@Data
@AllArgsConstructor
class ChatMessageInstance implements Comparable<ChatMessageInstance>,
        Transformable<ChatMessageModel> {

    /**
     * Author for the ChatMessage.
     */
    private final int userId;

    /**
     * Model for this ChatMessageInstance.
     */
    private final ChatMessageModel model;

    @Override
    public int compareTo(final ChatMessageInstance other) {
        return getModel().compareTo(other.getModel());
    }

    @Override
    public ChatMessageModel transform() {
        return model;
    }

    /**
     * Id for the ChatMessage.
     *
     * @return the id for the chat message
     */
    public Integer getId() {
        return getModel().getId();
    }

    /**
     * The actual message.
     *
     * @return The actual message of this chatmessage.
     */
    public String getMessage() {
        return getModel().getMessage();
    }

    /**
     * The timestamp the message was posted.
     *
     * @return The timestamp at which the chatmessage has been received by the server.
     */
    public Long getTimestamp() {
        return getModel().getTimestamp();
    }

    /**
     * Create a new ChatMessageInstance.
     *
     * @param chatMessage
     *            The ChatMessage to be created.
     * @return the created ChatMessageInstance.
     */
    public static ChatMessageInstance create(final ChatMessage chatMessage) {
        final ChatMessageModel model = new ChatMessageModel();
        model.setMessage(chatMessage.getMessage());
        model.setAuthor(chatMessage.getUser().getName());
        model.setTimestamp(chatMessage.getTimestamp());
        model.setId(chatMessage.getId());

        int userId = chatMessage.getUser().getId();
        return new ChatMessageInstance(userId, model);
    }

}
