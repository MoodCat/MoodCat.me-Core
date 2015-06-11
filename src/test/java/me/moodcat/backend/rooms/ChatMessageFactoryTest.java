package me.moodcat.backend.rooms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class ChatMessageFactoryTest {
	
	private static final String AUTHOR = "author";

	private static final int USER_ID = 1;

	@Mock
	private Provider<UserDAO> userDAOProvider;
	
	@InjectMocks
	private ChatMessageFactory factory;
	
	@Mock
	private Room room;
	
	private ChatMessageInstance instance;
	
	private ChatMessageModel model;

	@Mock
	private UserDAO userDAO;

	@Mock
	private User user;
	
	@Before
	public void setUp() {
		model = new ChatMessageModel();
		instance = new ChatMessageInstance(USER_ID, model);
		
		when(userDAOProvider.get()).thenReturn(userDAO);
		when(userDAO.findById(USER_ID)).thenReturn(user);
		
		when(user.getId()).thenReturn(USER_ID);
		when(user.getName()).thenReturn(AUTHOR);
		
		model.setMessage("message");
		model.setAuthor(AUTHOR);
		model.setId(1);
		model.setTimestamp(System.currentTimeMillis());
	}

	@Test
	public void canCreateChatMessage() {
		ChatMessage message = factory.create(room, instance);
		
		assertEquals(instance, ChatMessageInstance.create(message));
	}
}
