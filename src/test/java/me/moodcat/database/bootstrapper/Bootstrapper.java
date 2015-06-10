package me.moodcat.database.bootstrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The {@code Bootstrapper} loads an environment for a test
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class Bootstrapper {

    private final Map<Integer, Artist> persistedArtists;

    private final Map<Integer, Song> persistedSongs;

    private final Map<Integer, Room> persistedRooms;

    private final Map<Integer, User> persistedUsers;

    /**
     * ObjectMapper used for the bootstrapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * ArtistDAO.
     */
    private final ArtistDAO artistDAO;

    private final RoomDAO roomDAO;

    private final SongDAO songDAO;

    private final ChatDAO chatDAO;

    private final UserDAO userDAO;

    @Inject
    public Bootstrapper(final ObjectMapper objectMapper, final ArtistDAO artistDAO,
            final RoomDAO roomDAO, final SongDAO songDAO, final ChatDAO chatDAO,
            final UserDAO userDAO) {
        this.objectMapper = objectMapper;
        this.artistDAO = artistDAO;
        this.roomDAO = roomDAO;
        this.songDAO = songDAO;
        this.chatDAO = chatDAO;
        this.userDAO = userDAO;
        this.persistedArtists = Maps.newHashMap();
        this.persistedSongs = Maps.newHashMap();
        this.persistedRooms = Maps.newHashMap();
        this.persistedUsers = Maps.newHashMap();
    }

    /**
     * BEnvironment contains the artists and songs to be initialised.
     */
    @Data
    private static class BEnvironment {

        private List<BUser> users;

        private List<BArtist> artists;

        private List<BRoom> rooms;
    }

    @Data
    private static class BUser {

        private int id;

        private String username;

        private int soundCloudUserId;

        private Integer points;

    }

    /**
     * BArtist the artist with his songs.
     */
    @Data
    private static class BArtist {

        private int id;

        private String name;

        private List<BSong> songs;
    }

    /**
     * Song for an artist.
     */
    @Data
    private static class BSong {

        private int id;

        private String name;

        private String artworkUrl;

        private int soundCloudId;

        private int duration;
    }

    @Data
    private static class BRoom {

        private int id;

        private String name;

        private int position;

        private int time;

        private int songId;

        private List<BMessage> messages;
    }

    @Data
    private static class BMessage {

        private Integer userId;

        private String message;

        private long time;
    }

    /**
     * Parse environment from resource file.
     *
     * @param path
     *            path to resource
     * @throws IOException
     *             if an I/O error occurs
     */
    public void parseFromResource(final String path) throws IOException {
        try (InputStream in = Bootstrapper.class.getResourceAsStream(path)) {
            final BEnvironment environment = objectMapper.readValue(in, BEnvironment.class);
            environment.getUsers().forEach(this::createUser);
            environment.getArtists().forEach(this::createArtist);
            environment.getRooms().forEach(this::createRoom);
        }
    }

    @Transactional
    protected void createUser(BUser bUser) {
        final User user = new User();
        user.setId(bUser.getId());
        user.setName(bUser.getUsername());
        user.setSoundCloudUserId(bUser.getSoundCloudUserId());
        user.setPoints(bUser.getPoints());
        User persistedUser = userDAO.merge(user);
        persistedUsers.put(persistedUser.getId(), persistedUser);
        log.info("Bootstrapper created user {}", persistedUser);
    }

    @Transactional
    protected void createRoom(BRoom bRoom) {
        Room room = new Room();
        room.setId(bRoom.getId());
        room.setName(bRoom.getName());
        room.setChatMessages(Collections.<ChatMessage> emptyList());
        room.setCurrentSong(persistedSongs.get(bRoom.getSongId()));
        room.setVaVector(room.getCurrentSong().getValenceArousal());
        Room persistedRoom = roomDAO.merge(room);
        bRoom.getMessages().forEach(bSong -> createChatMessage(bSong, persistedRoom));
        persistedRooms.put(bRoom.getId(), persistedRoom);
        log.info("Bootstrapper created room {}", persistedRoom);
    }

    @Transactional
    protected ChatMessage createChatMessage(BMessage bMessage, Room room) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(persistedUsers.get(bMessage.getUserId()));
        chatMessage.setMessage(bMessage.getMessage());
        chatMessage.setTimestamp(bMessage.getTime());
        chatMessage.setRoom(room);
        return chatDAO.persist(chatMessage);
    }

    @Transactional
    protected Song createSong(BSong bSong, Artist artist) {
        Song song = new Song();
        song.setId(bSong.getId());
        song.setName(bSong.getName());
        song.setArtworkUrl(bSong.getArtworkUrl());
        song.setDuration(bSong.getDuration());
        song.setSoundCloudId(bSong.getSoundCloudId());
        song.setArtist(artist);
        song.setValenceArousal(new VAVector(0, 0));
        song = songDAO.merge(song);
        persistedSongs.put(bSong.getId(), song);
        return song;
    }

    @Transactional
    protected void createArtist(BArtist bArtist) {
        final Artist artist = new Artist();
        artist.setId(bArtist.getId());
        artist.setName(bArtist.getName());
        Artist persistedArtist = artistDAO.merge(artist);
        persistedArtists.put(bArtist.getId(), persistedArtist);
        bArtist.getSongs().forEach(bSong -> createSong(bSong, persistedArtist));
        log.info("Bootstrapper created artist {}", persistedArtist);
    }

    /**
     * Remove persisted entitites from the database.
     */
    public void cleanup() {
        persistedArtists.clear();
        persistedSongs.clear();
        persistedRooms.clear();
    }

    /**
     * Get a persisted artist
     * 
     * @param id
     *            id for the artist
     * @return artist
     */
    public Artist getArtist(Integer id) {
        return persistedArtists.get(id);
    }

    /**
     * Get a persisted Room
     * 
     * @param id
     *            id for the Room
     * @return Room
     */
    public Room getRoom(Integer id) {
        return persistedRooms.get(id);
    }

    /**
     * Get an artist
     *
     * @param id
     *            id for the Song
     * @return an artist
     */
    public Song getSong(Integer id) {
        return persistedSongs.get(id);
    }

}
