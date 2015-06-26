package me.moodcat.database.entities;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.moodcat.database.embeddables.VAVector;

/**
 * Classification entity.
 */
@Data
@Entity
@ToString
@Table(name = "classification", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_CLASSIFICATION_SONG_USER", columnNames = {
                "song_id", "user_id", "room_id"
        })
})
@EqualsAndHashCode(of = {
    "id"
})
public class Classification {

    /**
     * The unique id of this classification.
     *
     * @param id
     *            The new id to set.
     * @return The unique id of this classification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Song for this classification
     *
     * @param song
     *            The new song to set.
     * @return The song of this classification.
     */
    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /**
     * User that classified this song.
     *
     * @param user
     *            The new user to set.
     * @return The user of this classification.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Room that the user classified in. {@code Null} for the classification game.
     *
     * @param room
     *            The new room to set.
     * @return The room of this classification. {@code Null} for the classification game.
     */
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;

    /**
     * The valence and arousal vector of this classification.
     *
     * @param valenceArousal
     *            The new vector to set.
     * @return The valence-arousal vector of this classification.
     */
    @Embedded
    private VAVector valenceArousal;

}
