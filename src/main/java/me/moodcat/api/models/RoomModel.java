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
     * The name of the room model.
     *
     * @param name
     *            name for this room
     * @return name for this room
     */
    private String name;

    /**
     * The song currently playing in the room.
     *
     * @param song
     *            the song currently playing in the room
     * @return the song currently playing in the room
     */
    private NowPlaying nowPlaying;

    /**
     * The song for the current room.
     * 
     * @return the song for this room
     * @deprecated Replaced by the now playing field
     */
    @Deprecated
    public SongModel getSong() {
        return nowPlaying.getSong();
    }

    /**
     * The time in the room.
     * 
     * @return the song for this room
     * @deprecated Replaced by the now playing field
     */
    @Deprecated
    public Long getTime() {
        return nowPlaying.getTime();
    }

}
