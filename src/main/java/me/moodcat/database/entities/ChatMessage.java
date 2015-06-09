package me.moodcat.database.entities;

import javax.persistence.*;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A chat message for a room.
 */
@Data
@Entity
@Table(name = "chatmessage")
@ToString(of = {
        "room", "message", "user"
})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor()
public class ChatMessage implements Cloneable, Comparable<ChatMessage> {

    /**
     * Global chatmessage id.
     *
     * @param id
     *            The new id of this chatmessage.
     * @return The id of this chatmessage.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

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
     * The room the message was for.
     *
     * @param room
     *            The room to set.
     * @return The room that the message was posted into.
     */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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
     * Create a clone of this ChatMessage. While cloning objects
     * is usually bad, we use it as a way to deproxy Hibernate
     * entities.
     *
     * @return A clone of this object
     */
    @Override
    public ChatMessage clone() {
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTimestamp(this.getTimestamp());
        chatMessage.setUser(this.getUser());
        chatMessage.setMessage(this.getMessage());
        chatMessage.setRoom(room);
        return chatMessage;
    }

    /**
     * Compare function that allows chatmessages to be sorted.
     *
     * @param o another chatmessage
     * @return the ordering
     */
    @Override
    public int compareTo(final ChatMessage o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }
}
