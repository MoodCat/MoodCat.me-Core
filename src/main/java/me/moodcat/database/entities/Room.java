package me.moodcat.database.entities;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.moodcat.database.embeddables.VAVector;
import distanceMetric.DistanceMetric;

/**
 * A representation for a room, the room mainly supplies which song is currently listened by users
 * of the room and then position of the room.
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
     *
     * @param id
     *            The id to set.
     * @return The id of this room.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The current song of the room.
     *
     * @param song
     *            The song to set.
     * @return The currently played song in this room.
     */
    @ManyToOne
    @JoinColumn(name = "currentSong")
    private Song song;

    /**
     * The current position of the song in milliseconds.
     *
     * @param position
     *            The position of this song.
     * @return The position of the song in the queue.
     */
    private Integer position;

    /**
     * The name of the room.
     *
     * @param name
     *            The name to set.
     * @return The name of this room.
     */
    @Column(name = "name")
    private String name;

    /**
     * The time of the {@link #song} in order to 'jump' right into listening.
     * This is not persisted in the database due to the high rate of updating
     *
     * @param time
     *            The time the song is played on.
     * @return The time that this song is played at.
     */
    private int time;

    /**
     * The arousal value of this room.
     *
     * @param arousal
     *            The arousal component of this room.
     * @return The arousal of this room.
     */
    @Column(name = "arousal")
    private double arousal;

    /**
     * The valence value of this room.
     *
     * @param valence
     *            The valence component of this room.
     * @return The valence of this room.
     */
    @Column(name = "valence")
    private double valence;

    /**
     * The chat messages in the room.
     *
     * @param chatMessages
     *            The last chatmessages sent in this room.
     * @return A list of all last chatmessages sent in this room.
     */
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "room")
    private List<ChatMessage> chatMessages;

    /**
     * DistanceMetric to determine the distance between 2 rooms. Will take {@link Room#arousal} and
     * {@link Room#valence} to create vectors.
     */
    public static final class RoomDistanceMetric implements DistanceMetric<Room> {

        @Override
        public double distanceBetween(final Room room1, final Room room2) {
            final VAVector room1vector = new VAVector(room1.getValence(), room1.getArousal());
            final VAVector room2vector = new VAVector(room2.getValence(), room2.getArousal());
            return room1vector.distance(room2vector);
        }
    }

}
