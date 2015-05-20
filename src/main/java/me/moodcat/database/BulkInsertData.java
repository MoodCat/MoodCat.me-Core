package me.moodcat.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.soundcloud.SoundCloudException;
import me.moodcat.soundcloud.SoundCloudExtract;
import me.moodcat.soundcloud.SoundCloudTrack;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

/**
 * MockData inserts initial data in to a clean database.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class BulkInsertData {

    /**
     * The path to a newline seperated list of SoundCloud ids.
     */
    private static final String SOUNDCLOUD_ID_FILE_PATH = "./src/main/resources/soundcloud_ids";

    /**
     * The artist DAO provider.
     */
    private final Provider<ArtistDAO> artistDAOProvider;

    /**
     * The song DAO provider.
     */
    private final Provider<SongDAO> songDAOProvider;

    /**
     * The room DAO provider.
     */
    private final Provider<RoomDAO> roomDAOProvider;

    /**
     * The SoundCloud extraction API.
     */
    private final SoundCloudExtract soundCloudExtract;

    /**
     * Object to bulk insert a list of given songs into the databse.
     * 
     * @param artistDAOProvider
     *            the artist provider.
     * @param songDAOProvider
     *            the song provider.
     * @param roomDAOProvider
     *            the room provider.
     */
    @Inject
    public BulkInsertData(final Provider<ArtistDAO> artistDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final Provider<RoomDAO> roomDAOProvider) {
        this.artistDAOProvider = artistDAOProvider;
        this.songDAOProvider = songDAOProvider;
        this.roomDAOProvider = roomDAOProvider;
        soundCloudExtract = new SoundCloudExtract();
    }

    /**
     * Retrieve songs from the API and put the in the database.
     * 
     * @throws IOException
     *             when the file with song ids could not be parsed.
     * @throws SoundCloudException
     *             when the SoundCloud API was not called successfully.
     */
    @Transactional
    public void insertData() throws IOException, SoundCloudException {
        final ArtistDAO artistDAO = artistDAOProvider.get();
        final SongDAO songDAO = songDAOProvider.get();
        final VAVector defaultVector = new VAVector(0, 0);

        int[] soundCloudIds = readSoundCloudIds();
        for (int id : soundCloudIds) {
            try {
                SoundCloudTrack track = soundCloudExtract.extract(id);
                Artist artist = new Artist();
                artist.setName(track.getUser().getUsername());
                artistDAO.persist(artist);

                Song song = songToTrack(track, id, artist, defaultVector);
                songDAO.persist(song);
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Insert a given amount of random generated rooms into the database. This is used for testing
     * purposes.
     * 
     * @param nRooms
     *            the amount of rooms to generate.
     */
    public void insertRandomRooms(int nRooms) {
        final RoomDAO roomDAO = roomDAOProvider.get();
        final SongDAO songDAO = songDAOProvider.get();
        List<Song> songs = songDAO.listSongs();
        Random random = new Random();
        for (int i = 0; i < nRooms; i++) {
            Room room = new Room();
            System.out.println(songs.size());
            room.setCurrentSong(songs.get(random.nextInt(songs.size())));
            room.setRoomName("ROOM_STUB #" + i);
            room.setCurrentTime(0);
            room.setPosition(i);
            room.setChatMessages(Collections.<ChatMessage> emptyList());
            roomDAO.persist(room);
        }
    }

    /**
     * Helper method to create a {@link Song} given a {@link SoundCloudTrack} and an id,
     * {@link Artist} and {@link VAVector}.
     * 
     * @param track
     *            the given track.
     * @param id
     *            the given SoundCloud id.
     * @param artist
     *            the given {@link Artist}
     * @param vector
     *            the given {@link VAVector}
     * @return the resulting song.
     */
    private Song songToTrack(SoundCloudTrack track, int id, Artist artist, VAVector vector) {
        Song song = new Song();
        song.setName(track.getTitle());
        song.setSoundCloudId(id);
        song.setDuration(track.getDuration());
        song.setArtist(artist);
        song.setArtworkUrl(track.getArtworkUrl());
        song.setValenceArousal(vector);
        return song;
    }

    /**
     * Read and parse the file specified in SOUNDCLOUD_ID_FILE_PATH and return an int array.
     * 
     * @return the parsed int array.
     * @throws IOException
     *             when the file is not found or can not be read.
     */
    private int[] readSoundCloudIds() throws IOException {
        String soundcloudIdString = new String(Files.readAllBytes(Paths
                .get(SOUNDCLOUD_ID_FILE_PATH)));
        String[] soundcloudIds = soundcloudIdString.split("\n");
        int[] ids = new int[soundcloudIds.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.parseInt(soundcloudIds[i]);
        }
        return ids;
    }

    /**
     * Clear all entries in the room, song and artist tables.
     */
    @Transactional
    public void clear() {
        final ArtistDAO artistDAO = artistDAOProvider.get();
        final SongDAO songDAO = songDAOProvider.get();
        final RoomDAO roomDAO = roomDAOProvider.get();

        roomDAO.listRooms().forEach(roomDAO::remove);
        songDAO.listSongs().forEach(songDAO::remove);
        artistDAO.listArtists().forEach(artistDAO::remove);
    }

}
