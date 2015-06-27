package me.moodcat.database.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Id Class for ChatMessage.
 */
@Data
@Embeddable
@EqualsAndHashCode(of = {
    "id"
})
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEmbeddable implements Serializable {

    private static final long serialVersionUID = -1962128728359087383L;

    /**
     * The room the message was for.
     *
     * @param room
     *            The room to set.
     * @return The room that the message was posted into.
     */
    @Column(name = "room_id", nullable = false)
    private int roomId;

    /**
     * Global chatmessage id.
     *
     * @param id
     *            The new id of this chatmessage.
     * @return The id of this chatmessage.
     */
    @Column(name = "id")
    private int id;

}
