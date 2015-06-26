package me.moodcat.backend;

import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import java.math.BigInteger;
import java.util.Random;

/**
 * Abstract {@code BackendTest} for some shared logic for testing backends.
 */
public abstract class BackendTest {

    protected static final Random random = new Random();

    protected static ChatMessageModel createChatMessage() {
        final ChatMessageModel chatMessage = new ChatMessageModel();
        User user = new User();
        user.setName(randomString());
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
        song.setValenceArousal(VAVector.createRandomVector());
        song.setDuration((random.nextInt() + 1) * 1000);
        return song;
    }

    protected static String randomString() {
        return new BigInteger(130, random).toString(32);
    }
}
