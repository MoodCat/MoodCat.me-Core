package me.moodcat.api.models;

import lombok.Data;

/**
 * The model of the room.
 */
@Data
public class RoomModel {

    /**
     * The unique identifier for the room.
     */
    private Integer id;

    /**
     * The current song of the room model.
     *
     * @param song Song for this room
     * @return Song for this room
     */
    private SongModel song;

    /**
     * The name of the room model.
     *
     * @param name name for this room
     * @return name for this room
     */
    private String name;

    /**
     * Song time.
     *
     * @param time time for this room
     * @return time for this room
     */
    private Integer time;

}
