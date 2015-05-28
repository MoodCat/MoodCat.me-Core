package me.moodcat.database.bootstrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * The {@code Bootstrapper} loads an environment for a test
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class Bootstrapper {

    /**
     * List of persisted artists for clean up.
     */
    private final List<Artist> persistedArtists;

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

    @Inject
    public Bootstrapper(final ObjectMapper objectMapper, final ArtistDAO artistDAO,
            final RoomDAO roomDAO, final SongDAO songDAO) {
        this.objectMapper = objectMapper;
        this.artistDAO = artistDAO;
        this.persistedArtists = Lists.newArrayList();
        this.roomDAO = roomDAO;
        this.songDAO = songDAO;
    }

    /**
     * BEnvironment contains the artists and songs to be initialised.
     */
    @Data
    private static class BEnvironment {

        private List<BArtist> artists;

        private List<BRoom> rooms;
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

        private String author;

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
            environment.getArtists().forEach(this::createArtist);
            environment.getRooms().forEach(this::createRoom);
        }
    }

    protected void createRoom(final BRoom bRoom) {
        final Room room = new Room();
        room.setId(bRoom.getId());
        room.setName(bRoom.getName());
        room.setTime(bRoom.getTime());
        room.setPosition(bRoom.getPosition());
        room.setChatMessages(Collections.<ChatMessage> emptyList());
        room.setChatMessages(bRoom.getMessages().stream()
                .map(bSong -> createChatMessage(bSong, room))
                .collect(Collectors.toList()));
        room.setSong(songDAO.findById(bRoom.getSongId()));
        final Room persistedRoom = roomDAO.merge(room);
        log.info("Bootstrapper created room {}", persistedRoom);
    }

    protected static ChatMessage createChatMessage(final BMessage bMessage, final Room room) {
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor(bMessage.getAuthor());
        chatMessage.setMessage(bMessage.getMessage());
        chatMessage.setTimestamp(bMessage.getTime());
        chatMessage.setRoom(room);
        return chatMessage;
    }

    protected static Song createSong(final BSong bSong, final Artist artist) {
        final Song song = new Song();
        song.setId(bSong.getId());
        song.setName(bSong.getName());
        song.setArtworkUrl(bSong.getArtworkUrl());
        song.setDuration(600);
        song.setSoundCloudId(bSong.getSoundCloudId());
        song.setArtist(artist);
        return song;
    }

    protected void createArtist(final BArtist bArtist) {
        final Artist artist = new Artist();
        artist.setId(bArtist.getId());
        artist.setName(bArtist.getName());
        artist.setSongs(bArtist.getSongs().stream()
                .map(bSong -> createSong(bSong, artist))
                .collect(Collectors.toList()));
        final Artist persistedArtist = artistDAO.merge(artist);
        persistedArtists.add(persistedArtist);
        log.info("Bootstrapper created artist {}", persistedArtist);
    }

    /**
     * Remove persisted entitites from the database.
     */
    public void cleanup() {
        persistedArtists.clear();
    }

    /**
     * Get an artist
     *
     * @return an artist
     */
    public Artist getFirstArtist() {
        return persistedArtists.stream().findAny().get();
    }

}
