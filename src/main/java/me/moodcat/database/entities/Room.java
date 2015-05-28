package me.moodcat.database.entities;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.moodcat.database.embeddables.VAVector;
import distanceMetric.DistanceMetric;

import java.util.List;

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
    private Integer id;

    /**
     * The name of the room.
     */
    @Column(name = "roomName")
    private String roomName;

    @Embedded
    @JsonIgnore
    private VAVector valenceArousal;

    /**
     * The current song of the room.
     */
    @ManyToOne
    @JoinColumn(name = "currentSong")
    private Song currentSong;

    /*
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
     * The songs recently played in the roomProvider<ChatDAO> chatDAOProvider
     */
    @Column(name = "name")
    private String name;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "room_play_history", joinColumns = {
            @JoinColumn(name = "room_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "song_id", referencedColumnName = "id")
    })
    private List<Song> playHistory;

    /**
     * Development flag to temporarily repeat the current song in a room
     */
    @Column(name = "repeat")
    private boolean repeat;

    /**
     * The arousal value of this room.
     */
    @Column(name = "arousal")
    private double arousal;

    /**
     * The valence value of this room.
     */
    @Column(name = "valence")
    private double valence;

    /**
     * The chat messages in the room.
     */
    @JsonIgnore
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "room")
    private List<ChatMessage> chatMessages;

    /**
     * DistanceMetric to determine the distance between 2 rooms. Will take {@link Room#arousal} and
     * {@link Room#valence} to create vectors.
     *
     * @author Gijs Weterings
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
