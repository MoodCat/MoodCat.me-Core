package me.moodcat.backend.rooms;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.persist.Transactional;

/**
 * A bridge for operations between a running {@link RoomInstance} and its persisted {@link Room}.
 * All methods should be run in a {@code UnitOfWork}.
 */
@Slf4j
public class RoomInstanceInUnitOfWork {

    public static final int HISTORY_SIZE = 25;

    private final RoomDAO roomDAO;

    private final SongDAO songDAO;

    private final ChatMessageFactory chatMessageFactory;

    private final Room room;

    private final AtomicBoolean changed;

    @Inject
    public RoomInstanceInUnitOfWork(final RoomDAO roomDAO, final SongDAO songDAO,
            final ChatMessageFactory chatMessageFactory, @Assisted final Integer id) {
        this.roomDAO = roomDAO;
        this.songDAO = songDAO;
        this.chatMessageFactory = chatMessageFactory;
        this.room = roomDAO.findById(id);
        this.changed = new AtomicBoolean(false);
    }

    /**
     * Add a song to the history.
     *
     * @param song
     *            Song to add.
     */
    @Transactional
    public void addSongToHistory(final Song song) {
        final LinkedList<Song> history = Lists.newLinkedList(room.getPlayHistory());
        if (history.size() > HISTORY_SIZE - 1) {
            history.removeFirst();
        }
        history.add(song);
        room.setPlayHistory(history);
        log.info("Added song {} to history for room {}", song, room);
        this.changed.set(true);
    }

    /**
     * Exclude this song from the rooms.
     */
    @Transactional
    public void excludeRoomFromSong() {
        Song song = getCurrentSong();
        room.addExclusion(song);
        log.info("Added {} to the exclusions for {}", song, room);
        this.changed.set(true);
    }

    /**
     * Queue songs for this room.
     *
     * @param songs
     *            Songs to be queued.
     */
    @Transactional
    public void queue(final Collection<Song> songs) {
        this.room.getPlayQueue().addAll(songs);
        log.info("Queued songs {} for room {}", songs, room);
        this.changed.set(true);
    }

    /**
     * Schedule the next song. This performs a set of operations:
     * <ul>
     * <li>Add current song to history through
     * {@link RoomInstanceInUnitOfWork#addSongToHistory(Song)}.</li>
     * <li>Update the song queue through {@link RoomInstanceInUnitOfWork#updateSongQueue()}</li>
     * <li>Pop a song from the play queue, and {@link Room#setCurrentSong(Song) set} it as current
     * song.</li>
     * </ul>
     *
     * @return the scheduled song.
     */
    @Transactional
    public Song nextSong() {
        final Song previous = this.room.getCurrentSong();
        addSongToHistory(previous);
        updateSongQueue();
        final List<Song> queue = room.getPlayQueue();
        final Song next = queue.remove(0);
        room.setCurrentSong(next);
        log.info("Setting current song for {} to {}", room, next);
        this.changed.set(true);
        return next;
    }

    /**
     * If the play queue for this {@link Room} is empty, query new songs from the {@link SongDAO}.
     * If no results are found, reschedule the history.
     */
    @Transactional
    public void updateSongQueue() {
        final List<Song> playQueue = room.getPlayQueue();
        if (playQueue.isEmpty()) {
            final List<Song> newSongs = songDAO.findNewSongsFor(room);
            log.info("Adding new songs for room {}", room);
            playQueue.addAll(newSongs);
        }
        if (playQueue.isEmpty()) {
            final List<Song> playHistory = room.getPlayHistory();
            playQueue.addAll(playHistory);
            log.warn("No new songs found, replaying history for {}", room);
            playHistory.clear();
        }
        this.changed.set(true);
    }

    /**
     * Persist chat messages.
     *
     * @param messages
     *            Messages to persist.
     */
    @Transactional
    public void persistMessages(final Collection<ChatMessageInstance> messages) {
        Collection<ChatMessage> newMessages = messages.stream()
                .map(message -> chatMessageFactory.create(room, message))
                .collect(Collectors.toList());
        log.info("Persisting messages for room {}", room);
        room.getChatMessages().addAll(newMessages);
        this.changed.set(true);
    }

    /**
     * Persist changes to this Room instance
     */
    @Transactional
    public void merge() {
        if (this.changed.getAndSet(false)) {
            log.info("Persisting changes for room {}", room);
            this.roomDAO.merge(room);
        }
        else {
            log.debug("Room not changed");
        }
    }

    /**
     * Get the current song.
     *
     * @return the current song.
     */
    @Transactional
    public Song getCurrentSong() {
        return this.room.getCurrentSong();
    }

}
