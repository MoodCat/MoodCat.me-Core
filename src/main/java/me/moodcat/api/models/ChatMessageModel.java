package me.moodcat.api.models;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ChatMessageModel.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageModel implements Comparable<ChatMessageModel> {

    /**
     * Id for the ChatMessage.
     *
     * @param id
     *            The id for the chat message
     * @return the id for the chat message
     */
    private Integer id;

    /**
     * The actual message.
     *
     * @param message
     *            The actual message to set.
     * @return The actual message of this chatmessage.
     */
    private String message;

    /**
     * Author for the ChatMessage.
     */
    @Deprecated
    private String author;

    /**
     * The timestamp the message was posted.
     *
     * @param timestamp
     *            The timestamp to set.
     * @return The timestamp at which the chatmessage has been received by the server.
     */
    private Long timestamp;

    @Override
    public int compareTo(final ChatMessageModel other) {
        return getTimestamp().compareTo(other.getTimestamp());
    }

}
