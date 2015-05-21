package me.moodcat.database.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * TODO: Add explanation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@Table(name = "song")
@EqualsAndHashCode(of = {
        "id"
})
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "soundcloudID")
    private Integer soundCloudId;

    @ManyToOne
    @JoinColumn(name = "artist", nullable = true)
    private Artist artist;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration")
    private int duration;

    @Column(name = "artworkUrl")
    private String artworkUrl;

    @JsonIgnore
    @Embedded
    private VAVector valenceArousal;

    @JsonIgnore
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valence", column = @Column(name = "expected_valence")),
            @AttributeOverride(name = "arousal", column = @Column(name = "expected_arousal"))
    })
    private VAVector expectedValenceArousal;

    @JsonIgnore
    private int numberOfPositiveVotes;

    /**
     * Increase {@link #numberOfPositiveVotes}.
     */
    public void increaseNumberOfPositiveVotes() {
        this.numberOfPositiveVotes++;
    }

    /**
     * Decreases {@link #numberOfPositiveVotes}.
     */
    public void decreaseNumberOfPositiveVotes() {
        this.numberOfPositiveVotes--;
    }

}
