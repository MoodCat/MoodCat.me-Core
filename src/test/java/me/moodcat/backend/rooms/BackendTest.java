package me.moodcat.backend.rooms;

import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import java.math.BigInteger;
import java.util.Random;

/**
 * Abstract {@code BackendTest} for some shared logic for testing backends.
 */
public abstract class BackendTest {

    protected static final Random random = new Random();

    protected static ChatMessage createChatMessage() {
        final ChatMessage chatMessage = new ChatMessage();
        User user = new User();
        user.setName(randomString());
        chatMessage.setUser(user);
        chatMessage.setMessage(randomString());
        chatMessage.setTimestamp(System.currentTimeMillis());
        return chatMessage;
    }

    protected static Song createSong(final int id) {
        final Song song = new Song();
        final Artist artist = new Artist();
        artist.setId(id);
        artist.setName(randomString());
        song.setId(id);
        song.setArtist(artist);
        song.setName(randomString());
        song.setArtworkUrl(randomString());
        song.setDuration((random.nextInt() + 1) * 1000);
        return song;
    }

    protected static String randomString() {
        return new BigInteger(130, random).toString(32);
    }
}
