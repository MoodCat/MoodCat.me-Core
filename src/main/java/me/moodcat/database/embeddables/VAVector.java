package me.moodcat.database.embeddables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Valence/Arousal vector class
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VAVector {

    @Column(name = "valence")
    private double valence;

    @Column(name = "arousal")
    private double arousal;

    /**
     * Add this VAVector to another VAVector
     * @param other Another VAVector
     * @return a new VAVector
     */
    public VAVector add(final VAVector other) {
        return new VAVector(getValence() + other.getValence(), getArousal() + other.getArousal());
    }

    /**
     * Subtract another vector from this VAVector
     * @param other Another VAVector
     * @return a new VAVector
     */
    public VAVector subtract(final VAVector other) {
        return new VAVector(getValence() - other.getValence(), getArousal() - other.getArousal());
    }

    /**
     * Multiply this VAVector with another VAVector
     * @param other Another VAVector
     * @return a new VAVector
     */
    public VAVector multiply(final VAVector other) {
        return new VAVector(getValence() * other.getValence(), getArousal() * other.getArousal());
    }

    /**
     * Multiply this vector with a scalar
     * @param scalar
     * @return VAVector
     */
    public VAVector multiply(final double scalar) {
        return new VAVector(getValence() * scalar, getArousal() * scalar);
    }

    /**
     * Get the distance between two vectors
     * @param other Another VAVector
     * @return the distance
     */
    public double distance(final VAVector other) {
        double a = other.getValence() - getValence();
        double b = other.getArousal() - getArousal();
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * Get the distance between two vectors
     * @param a first vector
     * @param b second vector
     * @return the distance
     */
    public static double distance(final VAVector a, final VAVector b) {
        return a.distance(b);
    }

}
