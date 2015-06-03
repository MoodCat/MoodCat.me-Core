package me.moodcat.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

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

    @SuppressWarnings("unchecked")
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
        when(chatDAOProvider.get()).thenReturn(chatDAO);

        when(roomDAO.listRooms()).thenReturn(rooms);

        when(unitOfWorkFactory.create(Matchers.any())).thenAnswer(
                new Answer<CallableInUnitOfWork<Object>>() {

                    @Override
                    public CallableInUnitOfWork<Object> answer(final InvocationOnMock invocation)
                            throws Throwable {
                        final CallableInUnitOfWork<Object> callable = Mockito
                                .mock(CallableInUnitOfWork.class);

                        final Callable<Object> realCall = invocation.getArgumentAt(0,
                                Callable.class);
                        when(callable.call()).thenAnswer(new Answer<Object>() {

                            @Override
                            public Object answer(final InvocationOnMock invocation)
                                    throws Throwable {
                                return realCall.call();
                            }
                        });
                        return callable;
                    }

                });

        roomBackend = new RoomBackend(roomDAOProvider, unitOfWorkFactory, chatDAOProvider);
    }

    @Test
    public void canSuccesfullyInstantiateRoomInstances() {
        assertNotNull(roomBackend);
    }

    @Test
    public void listAllRooms() {
        assertEquals(rooms, roomBackend.listAllRooms());
    }

    @Test
    public void getCorrectRoom() {
        assertEquals(room, roomBackend.getRoom(1));
    }

    @Test
    public void canSendMessage() {
        roomBackend.getRoomInstance(1).sendMessage(chatMessage);

        assertTrue(messages.contains(chatMessage));
        verify(roomDAO, atLeastOnce()).merge(room);
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
        final RoomInstance instance = roomBackend.getRoomInstance(1);

        room.setPlayQueue(Lists.newArrayList());
        room.setRepeat(true);

        instance.playNext();

        assertNotEquals(room.getPlayHistory(), room.getPlayQueue());
    }
}
