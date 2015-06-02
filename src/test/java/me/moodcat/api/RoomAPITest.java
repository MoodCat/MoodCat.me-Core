package me.moodcat.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.moodcat.backend.chat.ChatBackend;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.mood.Mood;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RoomAPITest {

    @Mock
    private ChatBackend chatBackend;

    @InjectMocks
    private RoomAPI roomAPI;

    private List<Room> roomList;

    @Mock
    private Room oneRoom;

    @Mock
    private ChatBackend.ChatRoomInstance oneRoomInstance;

    @Mock
    private Room otherRoom;

    private List<ChatMessage> messagesList;

    @Spy
    private ChatMessage message;

    @Before
    public void setUp() {
        roomList = new ArrayList<Room>();
        roomList.add(oneRoom);
        roomList.add(otherRoom);

        messagesList = new ArrayList<ChatMessage>();
        messagesList.add(message);

        when(chatBackend.listAllRooms()).thenReturn(roomList);
        when(oneRoomInstance.getRoom()).thenReturn(oneRoom);
        when(chatBackend.getRoomInstance(1)).thenReturn(oneRoomInstance);

        when(oneRoom.getVaVector()).thenReturn(Mood.HAPPY.getVector());
        when(oneRoom.getId()).thenReturn(1);
        when(chatBackend.getRoom(1)).thenReturn(oneRoom);
        when(oneRoom.getChatMessages()).thenReturn(messagesList);

        when(otherRoom.getVaVector()).thenReturn(Mood.ANGRY.getVector());
        when(otherRoom.getId()).thenReturn(2);
        when(chatBackend.getRoom(2)).thenReturn(otherRoom);
    }

    @Test
    public void retrieveNearestRoomsContainsOneRoom() {
        final List<String> moods = Arrays.asList("Happy");

        final List<Room> result = this.roomAPI.getRooms(moods, 1);

        assertEquals(1, result.size());
        assertTrue(result.contains(oneRoom));
    }

    @Test
    public void retrieveNearestRoomsIsSorted() {
        final List<String> moods = Arrays.asList("Exciting");

        final List<Room> result = this.roomAPI.getRooms(moods, 2);

        assertEquals(oneRoom, result.get(0));
        assertEquals(otherRoom, result.get(1));
    }

    @Test
    public void retrieveCorrectRoom() {
        assertEquals(oneRoom, this.roomAPI.getRoom(1));
    }

    @Test
    public void retrieveMessages() {
        assertEquals(messagesList, this.roomAPI.getMessages(1));
    }

    @Test
    public void storeMessagePersistsDatabase() {
        this.roomAPI.postChatMessage(message, 1);

        verify(chatBackend.getRoomInstance(1)).sendMessage(message);
        assertEquals(oneRoom, message.getRoom());
    }
}
