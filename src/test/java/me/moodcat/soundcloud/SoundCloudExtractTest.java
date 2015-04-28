package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExtractTest {

    private static final String COOL_SONG = "https://soundcloud.com/pegboardnerds/"
            + "who-the-fuck-is-paul-mccartney-kanye-west-x-jennifer-lawrence-x-pegboard-nerds";

    private SoundCloudExtract extract;

    @Before
    public void setUp() {
        extract = new SoundCloudExtract();
    }

    @Test
    @SneakyThrows
    public void testRetrieveSong() {
        final SoundCloudTrack song = extract.extract(COOL_SONG);
        assertNotNull(song.getTitle());
        assertNotNull(song.getArtworkUrl());
        assertNotNull(song.getId());
    }

    @SneakyThrows
    @Test(timeout = 10000)
    public void integrationTest() {
        ListeningScheduledExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(4));
        List<ListenableFuture<?>> futures = Lists.newArrayList();
        for(int i = 0; i < 25; i++) {
            futures.add(executorService.submit(this::testRetrieveSong));
        }
        Futures.allAsList(futures).get();
        executorService.shutdownNow();
    }

}
