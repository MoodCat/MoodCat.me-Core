package me.moodcat.backend.rooms;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import lombok.Getter;
import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;
import me.moodcat.util.DataUtil;
import me.moodcat.util.ProviderProxy;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoomInstanceTest {

	private RoomInstance instance;

	@Mock
	private SongInstanceFactory songInstanceFactory;

	@Mock
	@Getter
	private RoomDAO roomDAO;

	@Spy
	private Provider<RoomDAO> roomDAOProvider = new ProviderProxy<>(this::getRoomDAO);

	@Mock
	@Getter
	private SongDAO songDAO;

	@Spy
    private Provider<SongDAO> songDAOProvider = new ProviderProxy<>(this::getSongDAO);

	@Mock
	private UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    @Mock
    private ChatMessageFactory chatMessageFactory;

	@Mock
	private ProfanityChecker profanityChecker;

	private Song song;
	private Room room;

	private final static DataUtil dataUtil = new DataUtil();

	@Before
	public void setUp() {
		room = dataUtil.createRoom();
		song = room.getCurrentSong();

		when(roomDAO.findById(room.getId())).thenReturn(room);

		when(songInstanceFactory.create(any())).thenAnswer(a -> {
			final SongInstance songInstance = mock(SongInstance.class);
			when(songInstance.getSong()).thenReturn(a.getArgumentAt(0, Song.class));
			return songInstance;
		});

        when(chatMessageFactory.create(any(), any())).thenReturn(mock(ChatMessage.class));
		instance = new RoomInstance(songInstanceFactory, roomDAOProvider,
				songDAOProvider, unitOfWorkSchedulingService, chatMessageFactory, profanityChecker, room);
	}

	@Test
	public void whenTooManyMessagesRemoveOneFromList() {
	    User mock = mock(User.class);
	    when(mock.getId()).thenReturn(1);
	    
		for (int i = 0; i < RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES + 1; i++) {
		    ChatMessageModel model = new ChatMessageModel();
		    model.setMessage(String.valueOf(i));
		    
			instance.sendMessage(model, mock);
		}

		assertEquals(RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES, instance
				.getMessages().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenOneUserSendsMessagesTooFastThrowsException() {
	    ChatMessageModel model = new ChatMessageModel();
        model.setMessage("Spam");
        
        User user = mock(User.class);
        when(user.getId()).thenReturn(1337);
	    
        for (int i = 0; i < 6; i++) {
	    instance.sendMessage(model, user);
        }
    }

	@Test
	public void testReplayHistoryOnNoResults() {
		when(songDAO.findForDistance(room.getVaVector(), RoomInstance.NUMBER_OF_SELECTED_SONGS))
			.thenReturn(Collections.emptyList());

		assertThat(room.getPlayHistory(), Matchers.empty());
		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(song, room.getCurrentSong());

		instance.playNext();

		assertThat(room.getPlayHistory(), Matchers.empty());
		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(song, room.getCurrentSong());
	}


	@Test
	public void testReplayHistoryOnResults() {
		Song newSong = dataUtil.createSong();
		stubFindForDistance(room, newSong);

		assertThat(room.getPlayHistory(), Matchers.empty());
		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(song, room.getCurrentSong());

		instance.playNext();
		instance.mergeRoom();

		assertThat(room.getPlayHistory(), Matchers.contains(song));
		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(newSong, room.getCurrentSong());
	}

	@Test
	public void testLimitHistory() {
		List<Song> history = ImmutableList.copyOf(Stream.generate(dataUtil::createSong)
			.limit(RoomInstance.NUMBER_OF_SELECTED_SONGS)
			.iterator());

		room.setPlayHistory(history);

		Song newSong = dataUtil.createSong();
		stubFindForDistance(room, newSong);

		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(song, room.getCurrentSong());

		instance.playNext();
		instance.mergeRoom();

		List<Song> expectedHistory = Stream
			.concat(history.stream().skip(1), Stream.of(song))
			.collect(Collectors.toList());


		assertEquals(expectedHistory, room.getPlayHistory());
		assertThat(room.getPlayQueue(), Matchers.empty());
		assertEquals(newSong, room.getCurrentSong());
	}

	private void stubFindForDistance(Room room, Song... songs) {
		when(songDAO.findForDistance(room.getVaVector(), RoomInstance.NUMBER_OF_SELECTED_SONGS))
			.thenReturn(Lists.newArrayList(songs));
	}

}
