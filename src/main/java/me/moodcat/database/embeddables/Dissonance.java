package me.moodcat.database.embeddables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
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
