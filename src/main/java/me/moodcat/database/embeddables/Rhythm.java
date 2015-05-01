package me.moodcat.database.embeddables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The rhythm information of the song.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rhythm {

    /**
     * The beats per minute of the song.
     */
    @Column(name = "bpm")
    @JsonProperty("bpm")
    private double bpm;

}
