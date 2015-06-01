package me.moodcat.database.entities;

import com.google.common.collect.Lists;
import me.moodcat.mood.Mood;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jaap Heijligers
 */
public class RoomTest {

    @Test
    public void testRoom() {
        Room room1 = createDefaultRoom();
        Room room2 = createDefaultRoom();
        assertEquals(room1, room2);
    }

    private Room createDefaultRoom() {
        Room room = new Room();
        room.setName("Stub room");
        ChatMessage message = new ChatMessage();
        message.setMessage("Message");
        List<ChatMessage> messageList = Lists.newArrayList(message);
        room.setChatMessages(messageList);
        room.setId(343233);

        Song song = new Song();
        song.setName("Stub song");
        room.setCurrentSong(song);
        room.setVaVector(Mood.HAPPY.getVector());
        return room;
    }

}