package me.moodcat.soundcloud;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExceptionTest {

    @Test
    public void testGetMessage() throws Exception {
        SoundCloudException exception = new SoundCloudException("Error");
        assertEquals(exception.getMessage(), "Error");
    }
}
