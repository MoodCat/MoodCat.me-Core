package me.moodcat.backend;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import me.moodcat.backend.RoomBackend.RoomInstance;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.util.CallableInUnitOfWork;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
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

    @Mock
    private CallableInUnitOfWork<Callable<?>> callable;

    @Mock
    private CallableInUnitOfWorkFactory unitOfWorkFactory;

    @Captor
    private ArgumentCaptor<Callable<?>> callableCaptor;

    private RoomBackend roomBackend;

    private ArrayList<ChatMessage> messages;

    private List<Song> songHistory;

    private List<Song> songFuture;

    private ChatMessage chatMessage = createChatMessage();

    private Song song1 = createSong(1);

    private Song song2 = createSong(2);

    @Before
    public void setUp() {
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

        when(unitOfWorkFactory.create(Matchers.any())).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgumentAt(0, Callable.class));

        roomBackend = new RoomBackend(roomDAOProvider, songDAOProvider, unitOfWorkFactory,
                new MockedExecutorService(4));

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

    static class MockedExecutorService extends ScheduledThreadPoolExecutor {

        public MockedExecutorService(final int corePoolSize) {
            super(corePoolSize);
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            final FutureTask<T> future = new FutureTask<T>(task);
            future.run();
            return future;
        }
    }
}
