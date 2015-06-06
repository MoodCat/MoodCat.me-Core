package me.moodcat.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The now playing model contains the current song and the time for a room.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NowPlaying {

    /**
     * The current time
     *
     * @param time the current time
     * @return the current time
     */
    private long time;

    /**
     * The current song.
     *
     * @param song the current song
     * @return the current song
     */
    private SongModel song;

}
