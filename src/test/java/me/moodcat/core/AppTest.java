package me.moodcat.core;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

public class AppTest {

    @Test
    public void appCanBeLaunched() throws IOException {
        assertNotNull(new App());
    }
}
