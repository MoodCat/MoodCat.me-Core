package me.moodcat.backend.rooms;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.entities.Song;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

/**
 * A song that is currently playing in a room.
 */
@Slf4j
public class SongInstance extends Observable {

    /**
	 * 
	 */
	private final RoomBackend roomBackend;

	/**
     * The current time of the room's song.
     */
    private final AtomicLong currentTime;

    /**
     * The duration of the song.
     */
    private final int duration;

    /**
     * Last update.
     */
    private final AtomicLong lastUpdate;

    /**
     * Song id for the song.
     */
    private final int songId;

    /**
     * Create a new song instance.
     *
     * @param song
     *            The song this instance presents.
     * @param roomBackend TODO
     */
    public SongInstance(RoomBackend roomBackend, final Song song) {
        this.roomBackend = roomBackend;
		Preconditions.checkNotNull(song);

        this.currentTime = new AtomicLong(0L);
        this.songId = song.getId();
        this.duration = song.getDuration();
        this.lastUpdate = new AtomicLong(System.currentTimeMillis());
    }

    @Transactional
    public Song getSong() {
        return this.roomBackend.getSongDAO().findById(songId);
    }

    /**
     * Method used to increment the time of the current song by one second.
     */
    protected void incrementTime() {
        if (isStopped() && !hasChanged()) {
            log.debug("Song {} has finished playing", this);
            setChanged();
            notifyObservers();
        } else {
            final long now = System.currentTimeMillis();
            final long then = lastUpdate.getAndSet(now);
            // log.debug("Incremented time for song {}", this);
            currentTime.addAndGet(now - then);
        }
    }

    /**
     * Check if the song has completed.
     *
     * @return true if the song is finished
     */
    public boolean isStopped() {
        final long time = currentTime.get();
        return time >= duration;
    }

    /**
     * Get the time.
     *
     * @return the time
     */
    public long getTime() {
        return currentTime.get();
    }

}