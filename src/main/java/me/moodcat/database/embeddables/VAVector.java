package me.moodcat.database.embeddables;

import java.util.List;
import java.util.Random;

import javax.persistence.Embeddable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Valence/Arousal vector class.
 */
@Data
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class VAVector {

    /**
     * The zero vector.
     */
    public static final VAVector ZERO;

    /**
     * The factory to create points.
     */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * The point that represents this vector.
     */
    @Type(type = "org.hibernate.spatial.GeometryType")
    private Point location;
    
    static {
        ZERO = new VAVector(0.0, 0.0);
    }

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
     * Set the valence for the {@code VAVector}.
     *
     * @param valence
     *            The valence value.
     */
    public void setValence(final double valence) {
        if (this.getLocation() == null) {
            this.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(valence, 0)));
        } else {
            this.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(valence, this.location
                    .getY())));
        }
    }

    /**
     * Set the arousal for the {@code VAVector}.
     *
     * @param arousal
     *            The arousal value.
     */
    public void setArousal(final double arousal) {
        if (this.getLocation() == null) {
            this.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(0, arousal)));
        } else {
            this.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(this.location.getX(),
                    arousal)));
        }
    }

    public double getValence() {
        return this.location.getX();
    }

    public double getArousal() {
        return this.location.getY();
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
        return distance(this, ZERO);
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
                .reduce(ZERO, (one, other) -> {
                    counter.increment();
                    return one.add(other);
                });

        return counter.average(average);
    }

    /**
     * Create a {@link VAVector} with random valence and arousal values.
     *
     * @return the random vector.
     */
    public static VAVector createRandomVector() {
        final Random random = new Random();
        final double valence = 2 * random.nextDouble() - 1d;
        final double arousal = 2 * random.nextDouble() - 1d;
        return new VAVector(valence, arousal);
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

            return ZERO;
        }
    }
}
