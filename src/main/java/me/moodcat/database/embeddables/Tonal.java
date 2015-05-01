package me.moodcat.database.embeddables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The tonal information of the song.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tonal {

    /**
     * The key
     */
    @Column(name = "key_key")
    @JsonProperty("key_key")
    private String keyKey;

    /**
     * The scale of the key.
     */
    @Column(name = "key_scale")
    @JsonProperty("key_scale")
    private String keyScale;

    /**
     * The strength of the key.
     */
    @Column(name = "key_strength")
    @JsonProperty("key_strength")
    private double keyStrength;

    /**
     * The tuning frequency of the song.
     */
    @Column(name = "tuning_frequency")
    @JsonProperty("tuning_frequency")
    private double tuningFrequency;

}
