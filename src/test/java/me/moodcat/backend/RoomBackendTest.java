package me.moodcat.backend;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import me.moodcat.backend.RoomBackend.RoomInstance;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoomBackendTest {

    @Mock
    private Provider<RoomDAO> roomDAOProvider;

    @Mock
    private Provider<ChatDAO> chatDAOProvider;

    @Mock
    private RoomDAO roomDAO;

    @Mock
    private ChatDAO chatDAO;

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

    @Mock
    private ChatMessage chatMessage;

    private ArrayList<ChatMessage> messages;

    private List<Song> songHistory;

    private List<Song> songFuture;

    @Mock
    private Song song1;

    @Mock
    private Song song2;

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

        when(song1.getDuration()).thenReturn(1000);

        when(room.getId()).thenReturn(1);
        when(room.getChatMessages()).thenReturn(messages);
        when(room.getPlayHistory()).thenReturn(songHistory);
        when(room.getPlayQueue()).thenReturn(songFuture);

        when(roomDAOProvider.get()).thenReturn(roomDAO);
        when(chatDAOProvider.get()).thenReturn(chatDAO);

        when(roomDAO.listRooms()).thenReturn(rooms);

        when(unitOfWorkFactory.create(Matchers.any())).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgumentAt(0, Callable.class));

//        roomBackend = new RoomBackend(roomDAOProvider, unitOfWorkFactory,
//                new MockedExecutorService(4), chatDAOProvider);
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

        assertNotEquals(song, room.getCurrentSong());
    }

    @Test
    public void playSongCreatesQueueWhenRepeating() {
        when(room.getPlayQueue()).thenReturn(Lists.newArrayList());
        room.setRepeat(true);

        final RoomInstance instance = roomBackend.getRoomInstance(1);

        instance.playNext();

        assertNotEquals(room.getPlayHistory(), room.getPlayQueue());
    }

    @Test
    public void playSongDoesNotContinueWhenNoHistoryOrFuture() {
        when(room.getPlayQueue()).thenReturn(Lists.newArrayList());
        when(room.getPlayHistory()).thenReturn(Lists.newArrayList());
        when(room.getCurrentSong()).thenReturn(null);
        room.setRepeat(true);

        final RoomInstance instance = roomBackend.getRoomInstance(1);

        instance.playNext();

        assertNull(instance.getCurrentSong());
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
