package me.moodcat.database.entities;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.List;
import java.util.Set;

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
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.moodcat.database.embeddables.VAVector;

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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The {@link VAVector} of the room.
     */
    @Embedded
    private VAVector vaVector;

    /**
     * The current song of the room.
     */
    @ManyToOne
    @JoinColumn(name = "currentSong")
    private Song currentSong;

    /**
     * Songs to be played.
     */
    @ManyToMany(fetch = EAGER, cascade = ALL)
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
     * The songs recently played in the roomProvider&lt;ChatDAO&gt; chatDAOProvider.
     */
    @ManyToMany(fetch = LAZY, cascade = ALL)
    @JoinTable(name = "room_play_history", joinColumns = {
            @JoinColumn(name = "room_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "song_id", referencedColumnName = "id")
    })
    private List<Song> playHistory;

    /**
     * The chat messages in the room.
     */
    @OrderBy("timestamp asc")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "room")
    private Set<ChatMessage> chatMessages;

}
