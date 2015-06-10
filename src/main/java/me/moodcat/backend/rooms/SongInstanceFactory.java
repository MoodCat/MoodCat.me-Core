package me.moodcat.backend.rooms;

import me.moodcat.database.entities.Song;

/**
 * Helper for Guice assisted inject to create SongInstances.
 */
public interface SongInstanceFactory {

    /**
     * Create a new {@link SongInstance}.
     *
     * @param song {@link Song} to create a {@code SongInstance} fpor
     * @return the newly created SongInstance
     */
    SongInstance create(Song song);

}
