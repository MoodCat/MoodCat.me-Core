package me.moodcat.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.api.models.NowPlaying;
import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;
import me.moodcat.backend.Vote;
import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.backend.rooms.RoomInstance;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class RoomAPITest {

    private static final long PLAYING_TIME = 5000;

    private static final int SOUNCLOUD_ID = 25;

    @Mock
    private RoomBackend roomBackend;

    @Mock
    private RoomDAO roomDAO;

    @Mock
    private Provider<User> currentUserProvider;

    @Mock
    private User user;

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

    private List<ChatMessageModel> messagesList;

    @Spy
    private ChatMessageModel message;
    
    @Spy
    private ChatMessageModel anotherMessage;

    @Mock
    private Song song;

    @Mock
    private User currentUser;

    @Before
    public void setUp() {
        roomList = new ArrayList<Room>();
        roomList.add(oneRoom);
        roomList.add(otherRoom);

        when(user.getId()).thenReturn(1);
        when(currentUserProvider.get()).thenReturn(user);

        when(oneRoom.getId()).thenReturn(1);
        when(oneRoom.getVaVector()).thenReturn(Mood.HAPPY.getVector());

        when(otherRoom.getId()).thenReturn(2);
        when(otherRoom.getVaVector()).thenReturn(Mood.ANGRY.getVector());
        when(song.getValenceArousal()).thenReturn(Mood.HAPPY.getVector());

        messagesList = new ArrayList<ChatMessageModel>();
        messagesList.add(message);
        messagesList.add(anotherMessage);
        
        when(message.getId()).thenReturn(1);
        when(anotherMessage.getId()).thenReturn(2);

        mockRoom(oneRoom, oneRoomInstance);
        mockRoom(otherRoom, otherRoomInstance);

        when(roomDAO.listRooms()).thenReturn(Lists.newArrayList(oneRoom, otherRoom));
        when(roomDAO.queryRooms(any(VAVector.class), eq(1))).thenReturn(Lists.newArrayList(oneRoom));
        when(roomDAO.queryRooms(any(VAVector.class), eq(2))).thenReturn(Lists.newArrayList(oneRoom, otherRoom));
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
    public void retrieveMessagesFromLatestChatMessages() {
        assertEquals(Lists.newArrayList(anotherMessage), this.roomAPI.getMessages(1, 1));
    }

    @Test
    public void storeMessagePersistsDatabase() {
        message.setMessage("Hello World!");
        
        this.roomAPI.postChatMessage(message, 1);

        verify(roomBackend.getRoomInstance(1)).sendMessage(message, user);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void sendingTooLongMessageThrowsException() {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < 1000; i++) {
            builder.append('w');
        }
        
        message.setMessage(builder.toString());
        
        this.roomAPI.postChatMessage(message, 1);
    }


    @Test
    public void canRetrieveCurrentTime() {
        NowPlaying playing = this.roomAPI.getCurrentTime(1);

        assertEquals(PLAYING_TIME, playing.getTime());
        assertEquals(SongModel.transform(song), playing.getSong());
    }

    @Test(expected = IllegalArgumentException.class)
    public void bogusVoteThrowsException() {
        roomAPI.voteSong(SOUNCLOUD_ID, "bogus");
    }
    
    @Test
    public void processLike() {
        this.testEqualVote("LIKE", Vote.LIKE);
    }
    
    @Test
    public void processDislike() {
        this.testEqualVote("DISLIKE", Vote.DISLIKE);
    }
    
    private void testEqualVote(String vote, Vote expectedType) {
        roomAPI.voteSong(1, vote);
        
        verify(oneRoomInstance).addVote(eq(user), eq(expectedType));
    }
}
