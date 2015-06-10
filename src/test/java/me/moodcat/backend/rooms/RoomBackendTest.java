package me.moodcat.backend.rooms;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import me.moodcat.backend.BackendTest;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.mocks.RoomInstanceFactoryMock;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
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

    private RoomBackend roomBackend;

    private ArrayList<ChatMessage> messages;

    private List<Song> songHistory;

    private List<Song> songFuture;

    private static ChatMessage chatMessage = createChatMessage();

    private final static Song song1 = createSong(1);

    private final static Song song2 = createSong(2);

    @Before
    public void setUp() {
        roomInstanceFactoryMock = new RoomInstanceFactoryMock(songDAOProvider, roomDAOProvider, unitOfWorkSchedulingService);

        rooms = Lists.newArrayList();
        rooms.add(room);

        messages = Lists.newArrayList();
        messages.add(chatMessage);

        songHistory = Lists.newArrayList();
        room.setCurrentSong(song1);

        songFuture = Lists.newArrayList();
        songFuture.add(song2);

        when(room.getId()).thenReturn(1);
        when(room.getChatMessages()).thenReturn(messages);
        when(room.getPlayHistory()).thenReturn(songHistory);
        when(room.getPlayQueue()).thenReturn(songFuture);

        when(roomDAOProvider.get()).thenReturn(roomDAO);
        when(songDAOProvider.get()).thenReturn(songDAO);

        when(roomDAO.listRooms()).thenReturn(rooms);
        when(roomDAO.findById(room.getId())).thenReturn(room);

        when(unitOfWorkSchedulingService.performInUnitOfWork(any())).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgumentAt(0, Callable.class).call());

        roomBackend = new RoomBackend(unitOfWorkSchedulingService, roomInstanceFactoryMock, roomDAOProvider);
        roomBackend.initializeRooms();
    }

    @Test
    public void canSuccesfullyInstantiateRoomInstances() {
        assertNotNull(roomBackend);
    }

    @Test
    public void canSendMessage() {
        final RoomInstance roomInstance = roomBackend.getRoomInstance(1);
        roomInstance.sendMessage(chatMessage);

        assertTrue(roomInstance.getMessages().contains(chatMessage));
    }

    @Test
    public void canRetrieveMessages() {
        final RoomInstance instance = roomBackend.getRoomInstance(1);
        instance.sendMessage(chatMessage);

        assertTrue(instance.getMessages().contains(chatMessage));
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
        room.setRepeat(true);

        final RoomInstance instance = roomBackend.getRoomInstance(1);

        instance.playNext();
        instance.merge();

        assertNotEquals(room.getPlayHistory(), room.getPlayQueue());
    }
    
}
