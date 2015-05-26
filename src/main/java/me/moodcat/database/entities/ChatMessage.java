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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A chat message for a room.
 *
 * @author JeremybellEU
 */
@Data
@Entity
@Table(name = "chatmessage")
@ToString(of = {
        "room", "message", "author"
})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor()
public class ChatMessage {

    /**
     * Global chatmessage id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    /**
     * The actual message.
     */
    @Column(name = "message", nullable = false)
    @NonNull
    private String message;

    /**
     * The sender of the message.
     */
    @Column(name = "author", nullable = false)
    @NonNull
    private String author;

    /**
     * The room the message was for.
     */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * The timestamp the message was posted.
     */
    @Column(name = "timestamp", nullable = true)
    private Long timestamp;

}
