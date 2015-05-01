package me.moodcat.database.embeddables;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * A data class to store the JSON elements in.
 */
@Data
@Embeddable
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcousticBrainzData {

    /**
     * The tonal element we use to store the data in.
     */
    @Embedded
    @JsonProperty("tonal")
    private Tonal tonal;

    /**
     * The low level element we use to store the data in.
     */
    @Embedded
    @JsonProperty("lowlevel")
    private LowLevel lowlevel;

    /**
     * The rhythm element to store the data in.
     */
    @Embedded
    @JsonProperty("rhythm")
    private Rhythm rhythm;

}
