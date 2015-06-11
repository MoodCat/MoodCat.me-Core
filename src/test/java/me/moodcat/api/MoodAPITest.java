package me.moodcat.api;

import static org.junit.Assert.assertArrayEquals;
import me.moodcat.mood.Mood;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MoodAPITest {
	
	@InjectMocks
	private MoodAPI moodAPI;
	
	@Test
	public void canRetrieveAllMoods() {
		assertArrayEquals(Mood.values(), moodAPI.getMoods());
	}

}
