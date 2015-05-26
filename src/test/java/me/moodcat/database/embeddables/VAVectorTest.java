package me.moodcat.database.embeddables;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test to make sure {@link VAVector vectors} are elementary.
 *
 * @author JeremybellEU
 */
public class VAVectorTest {

    @Test
    public void additionAndSubstractionReturnSameVector() {
        final VAVector one = new VAVector(0.5, 0.5);
        final VAVector other = new VAVector(0.25, 0.25);

        assertEquals(one, one.add(other).subtract(other));
    }

    @Test
    public void multiplicationTwoAndSubstractionReturnSameVector() {
        final VAVector one = new VAVector(0.5, 0.5);
        final double scalar = 2;

        assertEquals(one, one.multiply(scalar).subtract(one));
    }

    @Test
    public void multiplicationTwoDistanceIsOne() {
        final VAVector one = new VAVector(0.5, 0.0);
        final double scalar = 2;

        assertEquals(one.length(), one.distance(one.multiply(scalar)), 1E-5);
    }

    @Test
    public void averageOfList() {
        final VAVector one = new VAVector(0.5, 0.5);
        final VAVector second = new VAVector(1.0, 1.0);
        final VAVector otherOne = new VAVector(-0.5, -0.5);
        final VAVector otherSecond = new VAVector(-1.0, -1.0);

        final List<VAVector> vectors = new ArrayList<VAVector>();
        vectors.add(one);
        vectors.add(second);
        vectors.add(otherOne);
        vectors.add(otherSecond);

        assertEquals(new VAVector(0.0, 0.0), VAVector.average(vectors));
    }

    @Test
    public void averageOfEmptyListIsZeroVector() {
        final VAVector zero = new VAVector(0.0, 0.0);

        final List<VAVector> vectors = new ArrayList<VAVector>();

        assertEquals(zero, VAVector.average(vectors));
    }
}
