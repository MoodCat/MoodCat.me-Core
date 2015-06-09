package me.moodcat.backend;

/**
 * A {@link me.moodcat.database.entities.Song} can be either liked or disliked in a {@link RoomInstance}.
 *
 * @author JeremybellEU
 */
public enum Vote {

    /**
     * The user likes the song.
     */
    LIKE,

    /**
     * The user dislikes the song.
     */
    DISLIKE;

}
