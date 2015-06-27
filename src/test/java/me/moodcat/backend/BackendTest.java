package me.moodcat.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Abstract {@code BackendTest} for some shared logic for testing backends.
 */
public abstract class BackendTest {

    protected static final Random random = new Random();

    protected static User createUser() {
        User user = new User();
        user.setId(random.nextInt());
        user.setName(randomString());
        return user;
    }

    protected static ChatMessageModel createChatMessage(final User user) {
        final ChatMessageModel chatMessage = new ChatMessageModel();
        chatMessage.setMessage(randomString());
        chatMessage.setTimestamp(System.currentTimeMillis());
        return chatMessage;
    }

    protected static Room createRoom(final Song song) {
        final Room room = new Room();
        room.setId(random.nextInt());
        room.setCurrentSong(song);
        room.setChatMessages(Sets.newHashSet());
        room.setPlayQueue(Lists.newLinkedList());
        room.setPlayHistory(Lists.newArrayList());
        room.setVaVector(VAVector.createRandomVector());
        return room;
    }

    protected static Song createSong() {
        return createSong(random.nextInt());
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
        song.setDuration((int) (Math.round(Math.random() + 0.5) * TimeUnit.MINUTES.toMillis(5)));
        return song;
    }

    protected static String randomString() {
        return new BigInteger(130, random).toString(32);
    }
}
