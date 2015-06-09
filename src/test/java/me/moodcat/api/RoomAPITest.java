package me.moodcat.api;

import com.google.common.collect.Lists;

import me.moodcat.api.models.NowPlaying;
import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;
import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.backend.rooms.RoomInstance;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.mood.Mood;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoomAPITest {

    private static final long PLAYING_TIME = 5000;

	@Mock
    private RoomBackend roomBackend;

    @Mock
    private RoomDAO roomDAO;

    @InjectMocks
    private RoomAPI roomAPI;

    private List<Room> roomList;

    @Mock
    private Room oneRoom;

    @Mock
    private RoomInstance oneRoomInstance;

    @Mock
    private RoomInstance otherRoomInstance;

    @Mock
    private Room otherRoom;

    private List<ChatMessage> messagesList;

    @Spy
    private ChatMessage message;
    
    @Spy
    private Song song;

    @Before
    public void setUp() {
        roomList = new ArrayList<Room>();
        roomList.add(oneRoom);
        roomList.add(otherRoom);

        when(oneRoom.getId()).thenReturn(1);
        when(oneRoom.getVaVector()).thenReturn(Mood.HAPPY.getVector());

        when(otherRoom.getId()).thenReturn(2);
        when(otherRoom.getVaVector()).thenReturn(Mood.ANGRY.getVector());

        messagesList = new ArrayList<ChatMessage>();
        messagesList.add(message);

        mockRoom(oneRoom, oneRoomInstance);
        mockRoom(otherRoom, otherRoomInstance);
        when(roomDAO.listRooms()).thenReturn(Lists.newArrayList(oneRoom, otherRoom));
    }

    private void mockRoom(Room room, RoomInstance roomInstance) {
        when(roomBackend.getRoomInstance(room.getId())).thenReturn(roomInstance);
        when(roomInstance.getMessages()).thenReturn(messagesList);
        when(roomInstance.getCurrentSong()).thenReturn(song);
        when(roomInstance.getCurrentTime()).thenReturn(PLAYING_TIME);
    }

    @Test
    public void retrieveNearestRoomsContainsOneRoom() {
        final List<String> moods = Arrays.asList("Happy");

        final List<RoomModel> result = this.roomAPI.getRooms(moods, 1);

        assertEquals(1, result.size());
    }

    @Test
    public void retrieveNearestRoomsIsSorted() {
        final List<String> moods = Arrays.asList("Exciting");

        final List<RoomModel> result = this.roomAPI.getRooms(moods, 2);

        assertEquals(RoomAPI.transform(oneRoomInstance), result.get(0));
    }

    @Test
    public void retrieveCorrectRoom() {
        assertEquals(RoomAPI.transform(oneRoomInstance), this.roomAPI.getRoom(1));
    }

    @Test
    public void retrieveMessages() {
        assertEquals(messagesList, this.roomAPI.getMessages(1));
    }

    @Test
    public void storeMessagePersistsDatabase() {
        this.roomAPI.postChatMessage(message, 1);

        verify(roomBackend.getRoomInstance(1)).sendMessage(message);
    }
    
    @Test
    public void canRetrieveCurrentTime() {
    	NowPlaying playing = this.roomAPI.getCurrentTime(1);
    	
    	assertEquals(PLAYING_TIME, playing.getTime());
    	assertEquals(SongModel.transform(song), playing.getSong());
    }
}
