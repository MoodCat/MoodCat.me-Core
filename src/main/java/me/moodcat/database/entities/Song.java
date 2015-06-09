package me.moodcat.database.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.moodcat.database.embeddables.VAVector;

/**
 * A song that can be played.
 */
@Data
@Entity
@Table(name = "song")
@EqualsAndHashCode(of = {
        "id"
})
public class Song {

    /**
     * The unique id of the song.
     *
     * @param id
     *            The new Id to set.
     * @return The id of the song.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The corresponding soundcloud id, in order to be used by the frontend.
     *
     * @param soundCloudId
     *            The new SoundCloudId to set.
     * @return The SoundCloudId of this song.
     */
    @Column(name = "soundcloudID")
    private Integer soundCloudId;

    /**
     * The artist that composed this song.
     *
     * @param artist
     *            The new artist to set.
     * @return The artist that composed this song.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "artist", nullable = true)
    private Artist artist;

    /**
     * The textual name of the song.
     *
     * @param name
     *            The new name to set.
     * @return The textual name of the song.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The duration in seconds of the song.
     *
     * @param duration
     *            The new duration to set.
     * @return The duration in seconds of the song.
     */
    @Column(name = "duration")
    private int duration;

    /**
     * The link to the artwork image of the song.
     *
     * @param artworkUrl
     *            The new artworkURL to set.
     * @return The url to the artwork image.
     */
    @Column(name = "artworkUrl")
    private String artworkUrl;

    /**
     * The valence and arousal vector of this song.
     *
     * @param valenceArousal
     *            The new vector to set.
     * @return The valence-arousal vector of this song.
     */
    @Embedded
    private VAVector valenceArousal;

//    /**
//     * The amount of votes received. Can become negative if more people voted it negative than
//     * positive.
//     *
//     * @param numberOfPositiveVotes
//     *            The new amount of votes to set.
//     * @return The amount of netto votes that this song received.
//     */
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "artist", nullable = true)
//    private List<Room> exclusions;
//
//    public void addExclusionRoom(final Room room) {
//        this.getExclusions().add(room);
//    }

}
