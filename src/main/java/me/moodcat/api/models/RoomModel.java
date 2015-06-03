package me.moodcat.api.models;

import lombok.Data;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

/**
 * The model of the room.
 */
@Data
public class RoomModel {

    /**
     * The current song of the room model.
     */
    private Song currentSong;

    /**
     * The name of the room model.
     */
    private String name;

    /**
     * The constructor is used to create a room model given a {@link Room}.
     *
     * @param room
     *            the room to create the room model of.
     */
    public RoomModel(final Room room) {
        this.currentSong = room.getCurrentSong();
        this.name = room.getName();
    }

}
