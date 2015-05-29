package me.moodcat.database.entities;

import com.google.common.collect.Lists;
import me.moodcat.mood.Mood;
import org.junit.Test;

import java.util.List;

import junitx.extensions.EqualsHashCodeTestCase;

import com.google.common.collect.Lists;

/**
 * @author Jaap Heijligers
 */
public class RoomTest extends EqualsHashCodeTestCase {

    public RoomTest(final String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        final Room room = new Room();
        room.setName("Stub room");
        room.setTime(343);
        final ChatMessage message = new ChatMessage();
        message.setMessage("Message");
        final List<ChatMessage> messageList = Lists.newArrayList(message);
        room.setChatMessages(messageList);
        room.setId(343233);

        final Song song = new Song();
        song.setName("Stub song");
        room.setCurrentSong(song);
        room.setVaVector(Mood.HAPPY.getVector());
        return room;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        final Room room = new Room();
        room.setName("Stub room");
        room.setTime(343);
        final ChatMessage message = new ChatMessage();
        message.setMessage("Message");
        final List<ChatMessage> messageList = Lists.newArrayList(message);
        room.setChatMessages(messageList);
        room.setId(34323);

        final Song song = new Song();
        song.setName("Stub song");
        room.setSong(song);
        room.setPosition(43);
        room.setValence(0.3);
        room.setArousal(0.5);
        return room;
    }

}
