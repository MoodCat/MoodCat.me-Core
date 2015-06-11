package me.moodcat.backend;

import lombok.Getter;

/**
 * A {@link me.moodcat.database.entities.Song} can be either liked or disliked in a {@link RoomInstance}.
 *
 * @author JeremybellEU
 */
public enum Vote {

    /**
     * The user likes the song.
     */
    LIKE(1),

    /**
     * The user dislikes the song.
     */
    DISLIKE(-1);
    
    @Getter
    private int value;
    
    private Vote(final int value) {
        this.value = value;
    }
    
    

}
