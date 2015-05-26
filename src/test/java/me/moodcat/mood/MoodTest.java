package me.moodcat.mood;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import me.moodcat.database.embeddables.VAVector;

import org.junit.Test;

public class MoodTest {

    private static final double epsilon = 1E-5;

    @Test
    public void closestTo() {
        final Mood mood = Mood.ANGRY;
        final VAVector vector = mood.getVector();
        final VAVector translatedVector = vector.add(new VAVector(epsilon, epsilon));

        assertEquals(mood, Mood.closestTo(translatedVector));
    }

    @Test
    public void averageVector() {
        final List<String> moods = Arrays.asList("Exciting", "Happy");

        final VAVector average = Mood.createTargetVector(moods);

        // We are in the first quadrant.
        assertTrue(average.getArousal() > 0.0);
        assertTrue(average.getValence() > 0.0);
    }

    @Test
    public void averageVectorOfNonExistingMoodsReturnsZeroVector() {
        final List<String> moods = Arrays.asList("Non-existant", "Bogus");

        assertEquals(new VAVector(0.0, 0.0), Mood.createTargetVector(moods));
    }
}
