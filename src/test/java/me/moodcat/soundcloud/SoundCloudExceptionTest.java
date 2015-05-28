package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExceptionTest {

    @Test
    public void testGetMessage() {
        final SoundCloudException exception = new SoundCloudException("Error");
        assertEquals(exception.getMessage(), "Error");
    }

    @Test
    public void testWithThrowable() {
        NullPointerException nullPointerException = new NullPointerException();
        SoundCloudException exception = new SoundCloudException("hi", nullPointerException);
        assertEquals(nullPointerException, exception.getCause());
    }

}
