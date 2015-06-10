package me.moodcat.database.entities;

import java.util.Set;

import junitx.extensions.EqualsHashCodeTestCase;
import me.moodcat.mood.Mood;

import com.google.common.collect.Sets;

/**
 * @author Jaap Heijligers
 */
public class RoomTest extends EqualsHashCodeTestCase {

    public RoomTest(String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        Room room = new Room();
        room.setName("Stub room");
        ChatMessage message = new ChatMessage();
        message.setMessage("Message");
        Set<ChatMessage> messageList = Sets.newHashSet(message);
        room.setChatMessages(messageList);
        room.setId(343233);

        Song song = new Song();
        song.setName("Stub song");
        room.setCurrentSong(song);
        room.setVaVector(Mood.HAPPY.getVector());
        return room;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        Room room = new Room();
        room.setName("Stub room");
        ChatMessage message = new ChatMessage();
        message.setMessage("Message");
        Set<ChatMessage> messageList = Sets.newHashSet(message);
        room.setChatMessages(messageList);
        room.setId(343234);

        Song song = new Song();
        song.setName("Stub song");
        room.setCurrentSong(song);
        room.setVaVector(Mood.ANGRY.getVector());
        return room;
    }

}
