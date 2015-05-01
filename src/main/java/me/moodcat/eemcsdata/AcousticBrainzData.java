package me.moodcat.eemcsdata;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data class to store the JSON elements in.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcousticBrainzData {

    @JsonProperty("tonal")
    private Tonal tonal;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tonal {

        @JsonProperty("key_key")
        private String keyKey;

        @JsonProperty("key_scale")
        private String keyScale;

        @JsonProperty("key_strength")
        private float keyStrength;

        @JsonProperty("tuning_frequency")
        private float tuningFrequency;
    }

    @JsonProperty("lowlevel")
    private LowLevel lowlevel;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LowLevel {

        @JsonProperty("dissonance")
        private Dissonance dissonance;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Dissonance {

            @JsonProperty("mean")
            private float mean;
        }

        @JsonProperty("average_loudness")
        private float averageLoudness;
    }

    @JsonProperty("rhythm")
    private Rhythm rhythm;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rhythm {

        @JsonProperty("bpm")
        private float bpm;
    }

}
