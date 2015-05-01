package me.moodcat.database.embeddables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * The tonal information of the song.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class LowLevel {

    /**
     * The dissonance element we use to store the data in.
     */
    @Embedded
    @JsonProperty("dissonance")
    private Dissonance dissonance;

    /**
     * The average loudness of the song.
     */
    @Column(name = "average_loudness")
    @JsonProperty("average_loudness")
    private double averageLoudness;
}
