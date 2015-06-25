package me.moodcat.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import java.util.Random;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class DataUtil {

	private final static Random random = new Random();

	public Song createSong() {
		final Song song = new Song();
		song.setId(random.nextInt());
		return song;
	}

	public Song createSong(final String name) {
		final Song song = createSong();
		song.setName(name);
		song.setValenceArousal(VAVector.createRandomVector());
		return song;
	}

	public Room createRoom() {
		final Room room = new Room();
		room.setId(random.nextInt());
		room.setCurrentSong(createSong());
		room.setChatMessages(Sets.newHashSet());
		room.setPlayQueue(Lists.newLinkedList());
		room.setPlayHistory(Lists.newArrayList());
		room.setVaVector(VAVector.createRandomVector());
		return room;
	}

}
