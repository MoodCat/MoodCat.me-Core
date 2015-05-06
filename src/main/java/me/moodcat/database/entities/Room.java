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
 * A representation for a room, the room mainly supplies which song is currently listened by users
 * of the room and then position of the room.
 *
 * @author Jaap Heijligers
 */
@Data
@Entity
@Table(name = "room")
@ToString(of = {
        "id",
})
@EqualsAndHashCode(of = "id")
public class Room {

    /**
     * The unique identifier for the room.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /**
     * The current song of the room.
     */
    @ManyToOne
    @JoinColumn(name = "currentSong")
    private Song currentSong;

    /**
     * The current position of the song in milliseconds.
     */
    @Column(name = "position")
    private Integer position;
}
