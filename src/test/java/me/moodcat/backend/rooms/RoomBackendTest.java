package me.moodcat.backend.rooms;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.BackendTest;
import me.moodcat.backend.Vote;
import me.moodcat.backend.mocks.RoomInstanceFactoryMock;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;
import me.moodcat.util.MockedUnitOfWorkSchedulingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Spy
    private MockedUnitOfWorkSchedulingService unitOfWorkSchedulingService = new MockedUnitOfWorkSchedulingService();

    @Mock
    private ChatMessageFactory chatMessageFactory;

    @Mock
    private ProfanityChecker profanityChecker;

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
    public void setUp() throws ExecutionException, InterruptedException {
        unitOfWorkSchedulingService.lifeCycleStarting(null);
        unitOfWorkSchedulingService.lifeCycleStarted(null);

        roomInstanceFactoryMock = new RoomInstanceFactoryMock(songDAOProvider, roomDAOProvider,
                chatMessageFactory, unitOfWorkSchedulingService, profanityChecker);

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
        VAVector roomVector = new VAVector(0.5, 0.5);
        when(room.getVaVector()).thenReturn(roomVector);

        when(roomDAOProvider.get()).thenReturn(roomDAO);
        when(songDAOProvider.get()).thenReturn(songDAO);

        when(roomDAO.listRooms()).thenReturn(rooms);
        when(roomDAO.findById(room.getId())).thenReturn(room);
        
        when(songDAO.findForDistance(eq(roomVector), Matchers.anyLong())).thenReturn(songFuture);

        roomBackend = new RoomBackend(unitOfWorkSchedulingService, roomInstanceFactoryMock,
                roomDAOProvider);
        roomBackend.initializeRooms().get();
    }

    @After
    public void tearDown() {
        unitOfWorkSchedulingService.lifeCycleStopping(null);
        unitOfWorkSchedulingService.lifeCycleStopped(null);
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
    public void canStoreMessages() throws InterruptedException {
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

        Thread.sleep(500);
        verify(roomDAO).merge(eq(room));
        assertThat(room.getChatMessages(), contains(message));
    }
    
    @Test
    public void canPlayNextSong() throws InterruptedException {
        final RoomInstance instance = roomBackend.getRoomInstance(1);

        final Song song = room.getCurrentSong();
        instance.playNext();
        instance.merge();

        Thread.sleep(500);
        assertNotEquals(song, room.getCurrentSong());
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
