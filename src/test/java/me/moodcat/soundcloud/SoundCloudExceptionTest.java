package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExceptionTest {

    @Test
    public void testGetMessage() {
        SoundCloudException exception = new SoundCloudException("Error");
        assertEquals(exception.getMessage(), "Error");
    }
}
