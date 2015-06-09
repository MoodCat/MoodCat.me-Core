package me.moodcat.database.entities;

import com.google.common.collect.Lists;

import me.moodcat.mood.Mood;

import java.util.List;

import junitx.extensions.EqualsHashCodeTestCase;

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
        List<ChatMessage> messageList = Lists.newArrayList(message);
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
        List<ChatMessage> messageList = Lists.newArrayList(message);
        room.setChatMessages(messageList);
        room.setId(343234);

        Song song = new Song();
        song.setName("Stub song");
        room.setCurrentSong(song);
        room.setVaVector(Mood.ANGRY.getVector());
        return room;
	}

}