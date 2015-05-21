package me.moodcat.mood;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import me.moodcat.database.embeddables.VAVector;

/**
 * A mood represents a vector in the valence-arousal plane which will be attached to song.
 *
 * @author JeremybellEU
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Mood {

    // CHECKSTYLE:OFF
    ANGRY(new VAVector(-1.0, -1.0), "Angry"),
    CALM(new VAVector(0.0, 0.0), "Calm"),
    EXCITING(new VAVector(0.0, 0.0), "Exiting"),
    HAPPY(new VAVector(1.0, 1.0), "Happy"),
    NERVOUS(new VAVector(0.0, 0.0), "Nervous"),
    PLEASING(new VAVector(0.0, 0.0), "Pleasing"),
    PEACEFUL(new VAVector(0.0, 0.0), "Peaceful"),
    RELAXED(new VAVector(0.0, 0.0), "Relaxed"),
    SAD(new VAVector(0.0, 0.0), "Sad"),
    SLEEPY(new VAVector(0.0, 0.0), "Sleepy");

    // CHECKSTYLE:ON

    /**
     * The vector that represents this mood.
     */
    @Getter
    @JsonIgnore
    public final VAVector vector;

    /**
     * Readable name for the frontend
     */
    @Getter
    public final String name;

    Mood(final VAVector vector, final String name) {
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
}
