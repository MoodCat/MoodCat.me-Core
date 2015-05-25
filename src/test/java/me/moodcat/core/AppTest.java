package me.moodcat.core;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AppTest {

    @Test
    public void appCanBeLaunched() {
        assertNotNull(new App());
    }
}
