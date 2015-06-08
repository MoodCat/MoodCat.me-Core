package me.moodcat.database.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An artist composes an entity that owns a {@link Song}.
 */
@Data
@Entity
@Table(name = "artist")
@ToString(of = {
        "id", "name"
})
@EqualsAndHashCode(of = {
        "id"
})
public class Artist {

    /**
     * The unique id of this artist.
     *
     * @param id
     *            The new id to set.
     * @return The unique id of this artist.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The name of this artist.
     *
     * @param name
     *            The name to set.
     * @return The name of this artist.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The list of songs that this artist composed.
     *
     * @param songs
     *            The new list of songs to set.
     * @return The list of songs that this artist composed.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Song> songs;

}
