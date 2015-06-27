package me.moodcat.backend.rooms;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.UnitOfWorkSchedulingServiceImpl;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Song;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * A song that is currently playing in a room.
 */
public class SongInstance {

    /**
     * SongDAO provider for the context.
     */
    private final Provider<SongDAO> songDAOProvider;

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
     * Stopped.
     */
    private final AtomicBoolean stopped;

    /**
     * {@link UnitOfWorkSchedulingServiceImpl} to schedule tasks in a unit of work.
     */
    private final UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    /**
     * Song id for the song.
     */
    private final int songId;

    /**
     * Stop observers.
     */
    private final List<StopObserver> observers;

    @FunctionalInterface
    public interface StopObserver {

        /**
         * Called when this song has stopped.
         */
        void stopped();

    }

    /**
     * Create a new song instance.
     *
     * @param songDAOProvider
     *            The SongDAO provider.
     * @param song
     *            The song this instance presents.
     */
    @AssistedInject
    public SongInstance(final Provider<SongDAO> songDAOProvider,
            final UnitOfWorkSchedulingService unitOfWorkSchedulingService,
            @Assisted final Song song) {
        assert songDAOProvider != null;
        Preconditions.checkNotNull(song);
        this.songDAOProvider = songDAOProvider;
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;

        this.currentTime = new AtomicLong(0L);
        this.stopped = new AtomicBoolean(false);
        this.observers = Lists.newLinkedList();
        this.songId = song.getId();
        this.duration = song.getDuration();
        this.lastUpdate = new AtomicLong(System.currentTimeMillis());

        final ScheduledFuture<?> future = this.unitOfWorkSchedulingService
                .scheduleAtFixedRate(this::incrementTime, 1L, 1L, TimeUnit.SECONDS);

        // Observer: Stop the increment time task when the song is finished
        this.addObserver(() -> future.cancel(false));

    }

    /**
     * Add stoped observer.
     *
     * @param stopObserver
     *            Observer to be called when this song stops.
     */
    public void addObserver(final StopObserver stopObserver) {
        this.observers.add(stopObserver);
    }

    /**
     * Get the song for this song instance.
     *
     * @return
     *         The song that is playing
     */
    public Song getSong() {
        return this.songDAOProvider.get().findById(songId);
    }

    /**
     * Stop this song instance.
     */
    public void stop() {
        if (!this.stopped.getAndSet(true)) {
            this.observers.forEach(StopObserver::stopped);
        }
    }

    /**
     * Method used to increment the time of the current song by one second.
     */
    protected void incrementTime() {
        if (!this.stopped.get()) {
            final long now = System.currentTimeMillis();
            final long then = lastUpdate.getAndSet(now);
            final long cur = currentTime.addAndGet(now - then);

            if (cur > duration) {
                this.stop();
            }
        }
    }

    /**
     * Check if the song has completed.
     *
     * @return true if the song is finished
     */
    public boolean isStopped() {
        return this.stopped.get();
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
