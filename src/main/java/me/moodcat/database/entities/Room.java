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

//    /**
//     * The name of the room.
//     */
//    @Column(name = "name")
//    private String roomName;

    @Embedded
    @JsonIgnore
    private VAVector vaVector;

    /**
     * The current song of the room.
     */
    @ManyToOne
    @JoinColumn(name = "currentSong")
    @JsonProperty("song")
    private Song currentSong;

    /**
     * Songs to be played.
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
//    @Column(name = "repeat")
//    private boolean repeat;

    /**
     * The chat messages in the room.
     */
    @JsonIgnore
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "room")
    private List<ChatMessage> chatMessages;

    /**
     * DistanceMetric to determine the distance between 2 rooms. Will take {@link Room#vaVector} and
     * to create vectors.
     *
     * @author Gijs Weterings
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
