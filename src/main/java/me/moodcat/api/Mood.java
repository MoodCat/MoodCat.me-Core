package me.moodcat.api;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.Getter;
import me.moodcat.database.embeddables.VAVector;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A mood represents a vector in the valence-arousal plane which will be attached to song.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Mood {

    // CHECKSTYLE:OFF
    ANGRY(new VAVector(-0.6, 0.6), "Angry"),
    CALM(new VAVector(0.3, -0.9), "Calm"),
    EXCITING(new VAVector(0.4, 0.8), "Exciting"),
    HAPPY(new VAVector(0.7, 0.6), "Happy"),
    NERVOUS(new VAVector(-0.7, 0.4), "Nervous"),
    PLEASING(new VAVector(0.6, 0.3), "Pleasing"),
    PEACEFUL(new VAVector(0.5, -0.7), "Peaceful"),
    RELAXED(new VAVector(0.6, -0.3), "Relaxed"),
    SAD(new VAVector(-0.7, -0.2), "Sad"),
    SLEEPY(new VAVector(-0.2, -0.9), "Sleepy");

    // CHECKSTYLE:ON

    /**
     * List of all names that represent moods. Used in {@link #nameRepresentsMood(String)}.
     * By storing this once, we save a lot of unnecessary list creations.
     */
    private static final List<String> MOOD_NAMES = Arrays.asList(Mood.values()).stream()
            .map(moodValue -> moodValue.getName())
            .collect(Collectors.toList());

    /**
     * The vector that represents this mood.
     *
     * @return The vector of this mood.
     */
    @Getter
    @JsonIgnore
    private final VAVector vector;

    /**
     * Readable name for the frontend.
     *
     * @return The readable name of this mood.
     */
    @Getter
    private final String name;

    private Mood(final VAVector vector, final String name) {
        this.vector = vector;
        this.name = name;
    }

    /**
     * Get the mood that is closest to the given vector.
     *
     * @param vector
     *            The vector to determine the mood for.
     * @return The Mood that is closest to the vector.
     */
    public static Mood closestTo(final VAVector vector) {
        double distance = Double.MAX_VALUE;
        Mood mood = null;

        for (final Mood m : Mood.values()) {
            final double moodDistance = m.vector.distance(vector);

            if (moodDistance < distance) {
                distance = moodDistance;
                mood = m;
            }
        }

        return mood;
    }

    /**
     * Get the vector that represents the average of the provided list of moods.
     *
     * @param moods
     *            The textual list of moods.
     * @return The average vector, or the zero-vector if no moods were found.
     */
    public static VAVector createTargetVector(final List<String> moods) {
        final List<VAVector> actualMoods = moods.stream()
                .filter(Mood::nameRepresentsMood)
                .map(mood -> Mood.valueOf(mood.toUpperCase(Locale.ROOT)))
                .map(mood -> mood.getVector())
                .collect(Collectors.toList());

        return VAVector.average(actualMoods);
    }

    private static boolean nameRepresentsMood(final String mood) {
        return MOOD_NAMES.contains(mood);
    }
}
