package me.moodcat.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class MockData {

    private final ArtistDAO artistDAO;
    private final SongDAO songDAO;

    @Inject
    public MockData(Provider<ArtistDAO> artistDAOProvider, Provider<SongDAO> songDAOProvider, PersistService service) {
        service.start();
        this.artistDAO = artistDAOProvider.get();
        this.songDAO = songDAOProvider.get();
        insertMockData();
    }

    @Transactional
    private void insertMockData() {
        Artist fallOutBoy = new Artist();
        fallOutBoy.setName("Fall Out Boy");
        artistDAO.persist(fallOutBoy);

        Song song = new Song();
        song.setName("Thanks for the Memories");
        song.setArtist(fallOutBoy);
        songDAO.persist(song);
    }

}
