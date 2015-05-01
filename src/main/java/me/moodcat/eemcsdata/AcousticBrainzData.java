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

    /**
     * The tonal element we use to store the data in.
     */
    @JsonProperty("tonal")
    private Tonal tonal;

    /**
     * The tonal information of the song.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tonal {

        /**
         * The key of the key.
         */
        @JsonProperty("key_key")
        private String keyKey;

        /**
         * The scale of the key.
         */
        @JsonProperty("key_scale")
        private String keyScale;

        /**
         * The strength of the key.
         */
        @JsonProperty("key_strength")
        private float keyStrength;

        /**
         * The tuning frequency of the song.
         */
        @JsonProperty("tuning_frequency")
        private float tuningFrequency;
    }

    /**
     * The low level element we use to store the data in.
     */
    @JsonProperty("lowlevel")
    private LowLevel lowlevel;

    /**
     * The tonal information of the song.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LowLevel {

        /**
         * The dissonance element we use to store the data in.
         */
        @JsonProperty("dissonance")
        private Dissonance dissonance;

        /**
         * The dissonance information of the song.
         */
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Dissonance {

            /**
             * The mean of the song.
             */
            @JsonProperty("mean")
            private float mean;
        }

        /**
         * The average loudness of the song.
         */
        @JsonProperty("average_loudness")
        private float averageLoudness;
    }

    /**
     * The rhythm element to store the data in.
     */
    @JsonProperty("rhythm")
    private Rhythm rhythm;

    /**
     * The rhythm information of the song.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rhythm {

        /**
         * The beats per minute of the song.
         */
        @JsonProperty("bpm")
        private float bpm;
    }

}
