package me.moodcat.database.embeddables;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Valence/Arousal vector class.
 */
@Data
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class VAVector {

    /**
     * The valence in the range of [-1, 1]. It is the first dimension of this vector.
     *
     * @param valence
     *            The new valence to set.
     * @return The (first-dimension) valence of this vector
     */
    @Column(name = "valence")
    private double valence;

    /**
     * The arousal of the song in the range of [-1, 1]. It is the second dimension of this vector.
     *
     * @param arousal
     *            The new arousal to set.
     * @return The (second-dimension) arousal of this vector.
     */
    @Column(name = "arousal")
    private double arousal;

    /**
     * Constructor to create a vector. Asserts that the provided valence and arousal are in the
     * specified range.
     *
     * @param valence
     *            The valence of this vector.
     * @param arousal
     *            The arousal of this vector.
     */
    public VAVector(final double valence, final double arousal) {
        this.setValence(valence);
        this.setArousal(arousal);
    }

    /**
     * Add this VAVector to another VAVector.
     *
     * @param other
     *            Another VAVector
     * @return a new VAVector
     */
    public VAVector add(final VAVector other) {
        return new VAVector(this.getValence() + other.getValence(), this.getArousal()
                + other.getArousal());
    }

    /**
     * Subtract another vector from this VAVector.
     *
     * @param other
     *            Another VAVector
     * @return a new VAVector
     */
    public VAVector subtract(final VAVector other) {
        return new VAVector(this.getValence() - other.getValence(), this.getArousal()
                - other.getArousal());
    }

    /**
     * Multiply this VAVector with another VAVector.
     *
     * @param other
     *            Another VAVector
     * @return a new VAVector
     */
    public VAVector dotProduct(final VAVector other) {
        return new VAVector(this.getValence() * other.getValence(), this.getArousal()
                * other.getArousal());
    }

    /**
     * Multiply this vector with a scalar.
     *
     * @param scalar
     *            The multiplier.
     * @return VAVector
     */
    public VAVector multiply(final double scalar) {
        return new VAVector(this.getValence() * scalar, this.getArousal() * scalar);
    }

    /**
     * Get the distance between two vectors.
     *
     * @param other
     *            Another VAVector
     * @return the distance
     */
    public double distance(final VAVector other) {
        final double a = other.getValence() - this.getValence();
        final double b = other.getArousal() - this.getArousal();
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * Get the distance between two vectors.
     *
     * @param one
     *            first vector
     * @param other
     *            second vector
     * @return the distance
     */
    public static double distance(final VAVector one, final VAVector other) {
        return one.distance(other);
    }

    /**
     * Get the length of this vector.
     *
     * @return The length of this vector.
     */
    public double length() {
        return this.distance(new VAVector(0.0, 0.0));
    }

    /**
     * Get the average vector of the list of vectors.
     *
     * @param vectors
     *            The list of vectors.
     * @return The average.
     */
    public static VAVector average(final List<VAVector> vectors) {
        final Counter counter = new Counter();

        final VAVector average = vectors.stream()
                .reduce(new VAVector(0.0, 0.0), (one, other) -> {
                    counter.increment();
                    return one.add(other);
                });

        return counter.average(average);
    }

    /**
     * Helper class in order to reduce more easily in {@link VAVector#average(List)}.
     */
    private static final class Counter {

        /**
         * The actual counter.
         */
        private int counter;

        public Counter() {
            counter = 0;
        }

        public void increment() {
            counter++;
        }

        public VAVector average(final VAVector vector) {
            if (counter > 0) {
                return vector.multiply(1.0 / counter);
            }

            return new VAVector(0.0, 0.0);
        }
    }
}
