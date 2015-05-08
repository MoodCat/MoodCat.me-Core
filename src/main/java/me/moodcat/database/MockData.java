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
import com.google.inject.persist.Transactional;

// CHECKSTYLE:OFF
// Can be turned of as this is mocked data
/**
 * MockData inserts initial data in to a clean database
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class MockData {

    private final Provider<ArtistDAO> artistDAOProvider;

    private final Provider<SongDAO> songDAOProvider;

    private final Provider<RoomDAO> roomDAOProvider;

    @Inject
    public MockData(final Provider<ArtistDAO> artistDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final Provider<RoomDAO> roomDAOProvider) {
        this.artistDAOProvider = artistDAOProvider;
        this.songDAOProvider = songDAOProvider;
        this.roomDAOProvider = roomDAOProvider;
    }

    @Transactional
    public void insertMockData() {
        final ArtistDAO artistDAO = artistDAOProvider.get();
        final SongDAO songDAO = songDAOProvider.get();
        final RoomDAO roomDAO = roomDAOProvider.get();

        final Artist fallOutBoy = new Artist();
        fallOutBoy.setName("Fall Out Boy");
        artistDAO.persist(fallOutBoy);

        final Song song = new Song();
        song.setName("Thanks for the Memories");
        song.setArtist(fallOutBoy);
        song.setDuration(208762);
        song.setArtworkUrl("https://i1.sndcdn.com/artworks-000052078494-1b00cs-large.jpg");
        song.setSoundCloudId(99517116);
        songDAO.persist(song);

        final Room room = new Room();
        room.setCurrentSong(song);
        room.setPosition(4);
        room.setCurrentTime(42);
        room.setRoomName("Silver Ass Wankers");
        roomDAO.persist(room);
    }

}
