package me.moodcat.api.models;

import junitx.extensions.EqualsHashCodeTestCase;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

/**
 * @author Jaap Heijligers
 */
public class RoomModelTest extends EqualsHashCodeTestCase {

    public RoomModelTest(String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        Room room = new Room();
        Song song = new Song();
        song.setName("Song");
        room.setCurrentSong(song);
        room.setName("First room");
        RoomModel model = new RoomModel(room);
        return model;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        Room room = new Room();
        Song song = new Song();
        song.setName("Song");
        room.setCurrentSong(song);
        room.setName("Second room");
        RoomModel model = new RoomModel(room);
        return model;
    }
}
