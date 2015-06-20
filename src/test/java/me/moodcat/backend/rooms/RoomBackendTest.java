package me.moodcat.backend.rooms;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.BackendTest;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.Vote;
import me.moodcat.backend.mocks.RoomInstanceFactoryMock;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class RoomBackendTest extends BackendTest {

    @Mock
    private Provider<RoomDAO> roomDAOProvider;

    @Mock
    private Provider<SongDAO> songDAOProvider;

    @Mock
    private RoomDAO roomDAO;

    @Mock
    private SongDAO songDAO;

    @Spy
    private Room room;

    private List<Room> rooms;

    private RoomInstanceFactoryMock roomInstanceFactoryMock;

    @Mock
    private UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    @Mock
    private ChatMessageFactory chatMessageFactory;

    private RoomBackend roomBackend;

    private ArrayList<ChatMessageModel> messages;

    private List<Song> songHistory;

    private List<Song> songFuture;

    @Mock
    private User user;

    private static ChatMessageModel chatMessage = createChatMessage();

    private final static Song song1 = createSong(1);

    private final static Song song2 = createSong(2);

    @Before
    public void setUp() {
        roomInstanceFactoryMock = new RoomInstanceFactoryMock(songDAOProvider, roomDAOProvider,
                chatMessageFactory, unitOfWorkSchedulingService);

        rooms = Lists.newArrayList();
        rooms.add(room);

        messages = Lists.newArrayList();
        messages.add(chatMessage);

        songHistory = Lists.newArrayList();
        room.setCurrentSong(song1);

        songFuture = Lists.newArrayList();
        songFuture.add(song2);

        when(room.getId()).thenReturn(1);
        when(room.getChatMessages()).thenReturn(Sets.newHashSet());
        when(room.getPlayHistory()).thenReturn(songHistory);
        when(room.getPlayQueue()).thenReturn(songFuture);

        when(roomDAOProvider.get()).thenReturn(roomDAO);
        when(songDAOProvider.get()).thenReturn(songDAO);

        when(roomDAO.listRooms()).thenReturn(rooms);
        when(roomDAO.findById(room.getId())).thenReturn(room);

        when(unitOfWorkSchedulingService.performInUnitOfWork(any())).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgumentAt(0, Callable.class).call());

        roomBackend = new RoomBackend(unitOfWorkSchedulingService, roomInstanceFactoryMock,
                roomDAOProvider);
        roomBackend.initializeRooms();
    }

    @Test
    public void canSuccesfullyInstantiateRoomInstances() {
        assertNotNull(roomBackend);
    }

    @Test
    public void canSendMessage() {
        final RoomInstance instance = roomBackend.getRoomInstance(1);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1);

        instance.sendMessage(chatMessage, user);

        assertTrue(instance.getMessages().contains(chatMessage));
    }

    @Test
    public void canRetrieveMessages() {
        final RoomInstance instance = roomBackend.getRoomInstance(1);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1);

        instance.sendMessage(chatMessage, user);

        assertTrue(instance.getMessages().contains(chatMessage));
    }
    
    @Test
    public void canStoreMessages() {
        final RoomInstance instance = roomBackend.getRoomInstance(1);
        final ChatMessageModel model = new ChatMessageModel();
        model.setMessage("message");
        final ChatMessage message = mock(ChatMessage.class);
        
        when(chatMessageFactory.create(eq(room), argThat(new ArgumentMatcher<ChatMessageInstance>() {
            @Override
            public boolean matches(Object argument) {
                return ((ChatMessageInstance) argument).getModel().equals(model);
            }
        }))).thenReturn(message);
        
        instance.sendMessage(model, mock(User.class));
        
        instance.merge();
        
        verify(roomDAO).merge(eq(room));
        assertThat(room.getChatMessages(), contains(message));
    }
    
    @Test
    public void canPlayNextSong() {
        final RoomInstance instance = roomBackend.getRoomInstance(1);

        final Song song = room.getCurrentSong();
        instance.playNext();
        instance.merge();

        assertNotEquals(song, room.getCurrentSong());
    }

    @Test
    public void playSongCreatesQueueWhenRepeating() {
        when(room.getPlayQueue()).thenReturn(Lists.newArrayList());

        Song mockedSong = mock(Song.class);
        when(room.getPlayHistory()).thenReturn(Lists.newArrayList(mockedSong));
        room.setRepeat(true);

        final RoomInstance instance = roomBackend.getRoomInstance(1);

        instance.playNext();
        instance.merge();

        assertFalse(room.getPlayQueue().isEmpty());
    }

    @Test
    public void playSongRemovesHistoryWhenRepeating() {
        when(room.getPlayQueue()).thenReturn(Lists.newArrayList());

        Song mockedSong = mock(Song.class);
        when(room.getPlayHistory()).thenReturn(Lists.newArrayList(mockedSong));
        room.setRepeat(true);

        final RoomInstance instance = roomBackend.getRoomInstance(1);

        instance.playNext();
        instance.merge();

        assertTrue(room.getPlayHistory().isEmpty());
    }

    @Test
    public void playSongProcessVotesAndAddsRoomToExclusionWhenTooManyDislikes() {
        Song mockedSong = mock(Song.class);
        when(room.getCurrentSong()).thenReturn(mockedSong);

        final RoomInstance instance = roomBackend.getRoomInstance(1);
        instance.addVote(user, Vote.DISLIKE);
        instance.playNext();

        verify(mockedSong).addExclusionRoom(room);
    }

    @Test
    public void playSongProcessVotesDoesNotAddRoomToExclusionWhenNotEngouhDislikes() {
        Song mockedSong = Mockito.mock(Song.class);
        when(room.getCurrentSong()).thenReturn(mockedSong);

        final RoomInstance instance = roomBackend.getRoomInstance(1);
        instance.addVote(user, Vote.LIKE);
        instance.playNext();

        verify(mockedSong, never()).addExclusionRoom(room);
    }

}
