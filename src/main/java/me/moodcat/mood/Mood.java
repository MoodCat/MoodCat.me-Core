package me.moodcat.mood;

import lombok.Getter;
import me.moodcat.database.embeddables.VAVector;

/**
 * A mood represents a vector in the valence-arousal plane which will be attached to song.
 *
 * @author JeremybellEU
 */
public enum Mood {

    // CHECKSTYLE:OFF
    ANGRY(0.0, 0.0),
    CALM(0.0, 0.0),
    EXCITING(0.0, 0.0),
    HAPPY(0.0, 0.0),
    NERVOUS(0.0, 0.0),
    PLEASING(0.0, 0.0),
    PEACEFUL(0.0, 0.0),
    RELAXED(0.0, 0.0),
    SAD(0.0, 0.0),
    SLEEPY(0.0, 0.0);

    // CHECKSTYLE:ON

    /**
     * The vector that represents this mood.
     */
    @Getter
    private final VAVector vector;

    private Mood(final double valence, final double arousal) {
        this.vector = new VAVector(valence, arousal);
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
