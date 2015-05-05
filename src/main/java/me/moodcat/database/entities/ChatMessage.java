package me.moodcat.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A chat message for a room.
 *
 * @author JeremybellEU
 */
@Data
@Entity
@Table(name = "chatmessage")
@ToString(of = {
        "roomId", "message"
})
@EqualsAndHashCode(of = "id")
public class ChatMessage {

    /**
     * Global chatmessage id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The actual message.
     */
    @Column(name = "message", nullable = false)
    private String message;

    /**
     * The room the message was for.
     */
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room roomId;

    /**
     * The timestamp the message was posted.
     */
    @Column(name = "timestamp", nullable = true)
    private long timestamp;

}
