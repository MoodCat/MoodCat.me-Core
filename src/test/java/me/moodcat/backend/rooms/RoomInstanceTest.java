package me.moodcat.backend.rooms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class RoomInstanceTest {

	private RoomInstance instance;

	@Mock
	private SongInstanceFactory songInstanceFactory;

	@Mock
	private Provider<RoomDAO> roomDAOProvider;

	@Mock
	private UnitOfWorkSchedulingService unitOfWorkSchedulingService;

	@Mock
	private Room room;

	@Before
	public void setUp() {
		when(room.getCurrentSong()).thenReturn(mock(Song.class));		
		when(songInstanceFactory.create(any())).thenReturn(mock(SongInstance.class));

		instance = new RoomInstance(songInstanceFactory, roomDAOProvider,
				unitOfWorkSchedulingService, room);
	}

	@Test
	public void whenTooManyMessagesRemoveOneFromList() {
		for (int i = 0; i < RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES + 1; i++) {
			instance.sendMessage(mock(ChatMessage.class));
		}

		assertEquals(RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES, instance
				.getMessages().size());
	}
}
