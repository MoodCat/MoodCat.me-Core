package me.moodcat.database.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * A chat message for a room.
 */
@Data
@Entity
@Table(name = "chatmessage")
@ToString(of = {
        "compoundId", "room", "message", "user"
})
@EqualsAndHashCode(of = {
        "compoundId", "room"
})
@NoArgsConstructor
public class ChatMessage implements Comparable<ChatMessage> {

    /**
     * The room the message was for.
     *
     * @param room
     *            The room to set.
     * @return The room that the message was posted into.
     */
    @ManyToOne
    @MapsId("roomId")
    private Room room;

    /**
     * Global chatmessage id.
     *
     * @param compoundId
     *            The new id of this chatmessage.
     * @return The id of this chatmessage.
     */
    @EmbeddedId
    private ChatMessageEmbeddable compoundId;

    /**
     * The actual message.
     *
     * @param message
     *            The actual message to set.
     * @return The actual message of this chatmessage.
     */
    @Column(name = "message", nullable = false)
    @NonNull
    private String message;

    /**
     * The sender of the message.
     *
     * @param author
     *            The author to set.
     * @return The author that made this chatmessage
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The timestamp the message was posted.
     *
     * @param timestamp
     *            The timestamp to set.
     * @return The timestamp at which the chatmessage has been received by the server.
     */
    @Column(name = "timestamp", nullable = true)
    private Long timestamp;

    /**
     * Compare function that allows chatmessages to be sorted.
     *
     * @param other
     *            another chatmessage
     * @return the ordering
     */
    @Override
    public int compareTo(final ChatMessage other) {
        return getTimestamp().compareTo(other.getTimestamp());
    }

    /**
     * Set the id of the message.
     * 
     * @param id
     *            The new id of the message.
     */
    public void setId(final int id) {
        ChatMessageEmbeddable embeddable = getCompoundId();

        if (embeddable == null) {
            embeddable = new ChatMessageEmbeddable();
            setCompoundId(embeddable);
        }
        
        embeddable.setId(id);
    }

    public int getId() {
        return getCompoundId().getId();
    }
}
