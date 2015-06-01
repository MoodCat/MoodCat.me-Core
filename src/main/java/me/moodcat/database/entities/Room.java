package me.moodcat.database.entities;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.moodcat.database.embeddables.VAVector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import distanceMetric.DistanceMetric;

/**
 * A representation for a room, the room mainly supplies which song is currently listened by users
 * of the room and then position of the room.
 */
@Data
@Entity
@Table(name = "room")
@ToString(of = {
        "id", "song", "name"
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

    @Embedded
    @JsonIgnore
    private VAVector vaVector;

    /**
     * The current song of the room.
     *
     * @param song
     *            The song to set.
     * @return The currently played song in this room.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "currentSong")
    @JsonProperty("song")
    private Song currentSong;

    /**
     * Songs to be played
     */
    @ManyToMany
    @JoinTable(name = "room_play_queue", joinColumns = {
            @JoinColumn(name = "room_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "song_id", referencedColumnName = "id")
    })
    private List<Song> playQueue;

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
     * The songs recently played in the roomProvider<ChatDAO> chatDAOProvider.
     */
    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "room_play_history", joinColumns = {
            @JoinColumn(name = "room_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "song_id", referencedColumnName = "id")
    })
    private List<Song> playHistory;

    /**
     * Development flag to temporarily repeat the current song in a room.
     */
    @Column(name = "repeat")
    private boolean repeat;

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
    @JsonIgnore
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "room")
    private List<ChatMessage> chatMessages;

    /**
     * DistanceMetric to determine the distance between 2 rooms. Will take {@link Room#arousal} and
     * {@link Room#valence} to create vectors.
     *
     */
    public static final class RoomDistanceMetric implements DistanceMetric<Room> {

        @Override
        public double distanceBetween(final Room room1, final Room room2) {
            final VAVector room1vector = room1.getVaVector();
            final VAVector room2vector = room2.getVaVector();
            return room1vector.distance(room2vector);
        }
    }

}
