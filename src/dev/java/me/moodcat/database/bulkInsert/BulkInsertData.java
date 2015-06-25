package me.moodcat.database.bulkInsert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

/**
 * MockData inserts initial data in to a clean database.
 */
@Singleton
@Slf4j
public class BulkInsertData {

    /**
     * The maximum length of the history and queue.
     */
    private static final int MAX_QUEUE_LENGTH = 10;

    /**
     * The minimum length of the history and queue.
     */
    private static final int MIN_QUEUE_LENGTH = 3;

    /**
     * The default [0,0] VA vector.
     */
    private static final VAVector DEFAULT_VECTOR = new VAVector(0, 0);

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
     * Random object, to avoid having to instantiate at every call.
     */
    private final Random random;

    /**
     * Name generator to generate room names.
     */
    private final NameGenerator nameGenerator;

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
    @SneakyThrows
    public BulkInsertData(final Provider<ArtistDAO> artistDAOProvider,
            final Provider<SongDAO> songDAOProvider,
            final Provider<RoomDAO> roomDAOProvider) {
        this.artistDAOProvider = artistDAOProvider;
        this.songDAOProvider = songDAOProvider;
        this.roomDAOProvider = roomDAOProvider;
        soundCloudExtract = new SoundCloudExtract();
        this.random = new Random();
        nameGenerator = new NameGenerator();
    }

    /**
     * Retrieve songs from the API and put the in the database.
     *
     * @throws IOException
     *             when the file with song ids could not be parsed.
     * @throws SoundCloudException
     *             when the SoundCloud API was not called successfully.
     */
    public void insertData() throws IOException, SoundCloudException {
        final SongDAO songDAO = songDAOProvider.get();
        final Map<String, Artist> artistMap = new HashMap<>();

        final List<Integer> soundCloudIds = readSoundCloudIds();
        for (final Integer id : soundCloudIds) {
            try {
                final SoundCloudTrack track = soundCloudExtract.extract(id);
                final Artist artist = getOrPersistArtist(track.getUser().getUsername(), artistMap);
                final Song song = songToTrack(track, id, artist, DEFAULT_VECTOR);
                songDAO.persist(song);
            } catch (final Exception e) {
                log.warn("Track " + id + " could not be persisted.");
            }
        }
    }

    /**
     * Persist an artist in a database given a username.
     *
     * @param username
     *            the username of the artist.
     * @param artistMap
     *            the map with already persisted arists.
     */
    @Transactional
    private Artist getOrPersistArtist(final String username, final Map<String, Artist> artistMap) {
        final ArtistDAO artistDAO = artistDAOProvider.get();
        Artist artist;
        if (artistMap.containsKey(username)) {
            artist = artistMap.get(username);
        } else {
            artist = new Artist();
            artist.setName(username);
            artistDAO.persist(artist);
        }
        return artist;
    }

    /**
     * Insert a given amount of random generated rooms into the database. This is used for testing
     * purposes.
     *
     * @param numberOfRooms
     *            the amount of rooms to generate.
     */
    public void insertRandomRooms(final int numberOfRooms) {
        final RoomDAO roomDAO = roomDAOProvider.get();
        final SongDAO songDAO = songDAOProvider.get();
        final List<Song> songs = songDAO.listSongs();
        for (int i = 0; i < numberOfRooms; i++) {
            final Room room = new Room();
            room.setPlayHistory(getRandomSongList(songs));
            room.setPlayQueue(getRandomSongList(songs));
            room.setCurrentSong(songs.get(random.nextInt(songs.size())));
            try {
                room.setName(nameGenerator.generate());
            } catch (IOException e) {
                log.error("Couldn't generate name for room {}", i, e);
                room.setName("ROOM_STUB #" + i);
            }
            room.setVaVector(VAVector.createRandomVector());
            room.setChatMessages(Collections.<ChatMessage> emptySet());
            roomDAO.persist(room);
        }
    }

    /**
     * Generate a random list of random length of songs. The bounds of the length of the list are
     * {@link BulkInsertData#MIN_QUEUE_LENGTH} and {@link BulkInsertData#MAX_QUEUE_LENGTH}.
     *
     * @param allSongs
     *            a list of all songs.
     * @return the generated list.
     */
    private List<Song> getRandomSongList(final List<Song> allSongs) {
        final int queueSize = MIN_QUEUE_LENGTH
                + random.nextInt(MAX_QUEUE_LENGTH - MIN_QUEUE_LENGTH);
        final List<Song> songs = Lists.newArrayList();
        for (int i = 0; i < queueSize; i++) {
            songs.add(getRandomSong(allSongs));
        }
        return songs;
    }

    /**
     * Get a single random song from the given list of songs.
     *
     * @param allSongs
     *            the list of all songs.
     * @return the random song.
     */
    private Song getRandomSong(final List<Song> allSongs) {
        return allSongs.get(random.nextInt(allSongs.size()));
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
    private Song songToTrack(final SoundCloudTrack track, final int id, final Artist artist,
            final VAVector vector) {
        final Song song = new Song();
        song.setName(track.getTitle());
        song.setSoundCloudId(id);
        song.setDuration(track.getDuration());
        song.setArtist(artist);
        song.setPurchaseTitle(track.getPurchaseTitle());
        song.setPurchaseUrl(track.getPurchaseUrl());
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
    private List<Integer> readSoundCloudIds() throws IOException {
        final String soundCloudIdString = new String(Files.readAllBytes(Paths
                .get(SOUNDCLOUD_ID_FILE_PATH)));
        return Arrays.asList(soundCloudIdString.split("\n")).stream()
                .map(Integer::valueOf).collect(Collectors.toList());
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
