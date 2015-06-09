package me.moodcat.backend.mocks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import me.moodcat.backend.rooms.SongInstance;
import me.moodcat.backend.rooms.SongInstanceFactory;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Song;

/**
 * Created by jwgmeligmeyling on 9-6-15.
 */
public class SongInstanceFactoryMock implements SongInstanceFactory {

    private final Provider<SongDAO> songDAOProvider;

    @Inject
    public SongInstanceFactoryMock(final Provider<SongDAO> songDAOProvider) {
        this.songDAOProvider = songDAOProvider;
    }

    @Override
    public SongInstance create(final Song song) {
        return new SongInstance(songDAOProvider, song);
    }
}
