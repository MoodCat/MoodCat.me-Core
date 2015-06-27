package me.moodcat.backend.rooms;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.BackendTest;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.Vote;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.ChatMessageEmbeddable;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;
import me.moodcat.util.JukitoRunnerSupportingMockAnnotations;
import me.moodcat.util.MockedUnitOfWorkSchedulingService;

import org.jukito.UseModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(JukitoRunnerSupportingMockAnnotations.class)
@UseModules(RoomBackendTest.RoomBackendTestModule.class)
public class RoomBackendTest extends BackendTest {

    private static UserDAO userDAO = Mockito.mock(UserDAO.class);

    private static RoomDAO roomDAO = Mockito.mock(RoomDAO.class);

    private static SongDAO songDAO = Mockito.mock(SongDAO.class);

    public static class RoomBackendTestModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new RoomBackendModule());
            bind(SongDAO.class).toInstance(songDAO);
            bind(RoomDAO.class).toInstance(roomDAO);
            bind(UserDAO.class).toInstance(userDAO);
            bind(UnitOfWorkSchedulingService.class).to(MockedUnitOfWorkSchedulingService.class);
        }
    }

    @Spy
    private Room room;

    private List<Room> rooms;

    @Inject
    private MockedUnitOfWorkSchedulingService unitOfWorkSchedulingService;

    @Mock
    private ChatMessageFactory chatMessageFactory;

    @Mock
    private ProfanityChecker profanityChecker;

    @Inject
    private RoomBackend roomBackend;

    private ArrayList<ChatMessageModel> messages;

    private List<Song> songHistory;

    private List<Song> songFuture;

    private final static User user = createUser();

    private final static ChatMessageModel chatMessage = createChatMessage(user);

    private final static Song song1 = createSong(1);

    private final static Song song2 = createSong(2);

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        rooms = Lists.newArrayList();
        rooms.add(room);

        messages = Lists.newArrayList();
        messages.add(chatMessage);

        songHistory = Lists.newArrayList();

        songFuture = Lists.newArrayList();
        songFuture.add(song2);

        room.setCurrentSong(song1);
        room.setExclusions(Lists.newArrayList());
        when(room.getId()).thenReturn(1);
        when(room.getChatMessages()).thenReturn(Sets.newHashSet());
        when(room.getPlayHistory()).thenReturn(songHistory);
        when(room.getPlayQueue()).thenReturn(songFuture);
        VAVector roomVector = new VAVector(0.5, 0.5);
        when(room.getVaVector()).thenReturn(roomVector);

        when(userDAO.findById(user.getId())).thenReturn(user);
        when(roomDAO.listRooms()).thenReturn(rooms);
        when(roomDAO.findById(room.getId())).thenReturn(room);

        when(songDAO.findForDistance(eq(roomVector), Matchers.anyLong())).thenReturn(songFuture);

        roomBackend.initializeRooms().get();
    }

    @After
    public void tearDown() {
        unitOfWorkSchedulingService.shutdownNow();
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
    public void canStoreMessages() throws InterruptedException, ExecutionException {
        final RoomInstance instance = roomBackend.getRoomInstance(1);
        final ChatMessageModel model = new ChatMessageModel();
        model.setMessage("message");

        instance.sendMessage(model, user);
        instance.merge().get();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(room);
        chatMessage.setMessage(model.getMessage());
        chatMessage.setTimestamp(model.getTimestamp());
        chatMessage.setCompoundId(new ChatMessageEmbeddable(room.getId(), model.getId()));
        chatMessage.setUser(user);

        verify(roomDAO, atLeast(1)).merge(eq(room));
        assertThat(room.getChatMessages(), contains(chatMessage));
    }

    @Test
    public void canPlayNextSong() throws InterruptedException, ExecutionException {
        final RoomInstance instance = roomBackend.getRoomInstance(1);
        final Song song = room.getCurrentSong();
        songHistory.add(createSong());

        instance.playNext();
        instance.merge().get();

        assertNotEquals(song, room.getCurrentSong());
    }

    @Test
    public void playSongProcessVotesAndAddsRoomToExclusionWhenTooManyDislikes()
            throws InterruptedException, ExecutionException {
        Song mockedSong = mock(Song.class);
        when(room.getCurrentSong()).thenReturn(mockedSong);

        final RoomInstance instance = roomBackend.getRoomInstance(1);
        instance.addVote(user, Vote.DISLIKE);
        instance.playNext().get();

        verify(room).addExclusion(mockedSong);
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
