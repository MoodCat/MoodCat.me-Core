package me.moodcat.backend.rooms;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Observer;

import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Song;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class SongInstanceTest {

	private static final int DURATION = 1;

	private SongInstance instance;

	@Mock
	private Provider<SongDAO> songDAOProvider;

	@Mock
	private Song song;

	@Mock
	private Observer observer;

	@Before
	public void setUp() {
		when(song.getDuration()).thenReturn(DURATION);

		instance = new SongInstance(songDAOProvider, song);
		instance.addObserver(observer);
	}

	@Test
	public void canIncrementTime() throws InterruptedException {
		// Wait a little bit to trigger the changed.
		Thread.sleep(DURATION * 10);

		// Trigger changed
		instance.incrementTime();

		// And verify it is changed now
		instance.incrementTime();

		verify(observer).update(eq(instance), any());
		assertTrue(instance.getTime() > DURATION);
	}

}
