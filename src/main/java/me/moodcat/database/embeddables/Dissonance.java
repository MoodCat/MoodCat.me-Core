package me.moodcat.database.embeddables;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The dissonance information of the song.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dissonance {

    /**
     * The mean of the song.
     */
    @Column(name = "mean_dissonance")
    @JsonProperty("mean")
    private double mean;

}
