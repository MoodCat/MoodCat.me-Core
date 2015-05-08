package me.moodcat.database;

import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;

/**
 * TODO: Add explanation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class MockData {

    private final ArtistDAO artistDAO;

    private final SongDAO songDAO;

    private final RoomDAO roomDAO;

    @Inject
    public MockData(final Provider<ArtistDAO> artistDAOProvider,
                    final Provider<SongDAO> songDAOProvider,
                    final Provider<RoomDAO> roomDAOProvider,
                    final PersistService service) {
        service.start();
        this.artistDAO = artistDAOProvider.get();
        this.roomDAO = roomDAOProvider.get();
        this.songDAO = songDAOProvider.get();
        this.insertMockData();
    }

    @Transactional
    private void insertMockData() {
        final Artist fallOutBoy = new Artist();
        fallOutBoy.setName("Fall Out Boy");
        this.artistDAO.persist(fallOutBoy);

        final Song song = new Song();
        song.setName("Thanks for the Memories");
        song.setArtist(fallOutBoy);
        song.setDuration(208762);
        song.setArtworkUrl("https://i1.sndcdn.com/artworks-000052078494-1b00cs-large.jpg");
        song.setSoundCloudId(99517116);
        this.songDAO.persist(song);

        final Room room = new Room();
        room.setCurrentSong(song);
        room.setPosition(4);
        room.setCurrentTime(42);
        room.setRoomName("Silver Ass Wankers");
        this.roomDAO.persist(room);
    }

}
