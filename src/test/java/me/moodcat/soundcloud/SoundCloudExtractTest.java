package me.moodcat.soundcloud;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Executors;

import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExtractTest extends SoundCloudAPIConnectorTest {

    private static final String COOL_SONG = "https://soundcloud.com/pegboardnerds/"
            + "who-the-fuck-is-paul-mccartney-kanye-west-x-jennifer-lawrence-x-pegboard-nerds";

    private static final String SONG2_INFO_URL = "https://api.soundcloud.com/resolve.json"
            + "?url=https://soundcloud.com/katfyr/binary-original-mix&client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

    private static final String STREAM_JSON_REPRESENTATION =
            new StringBuilder()
                    .append("{")
                    .append("\"http_mp3_128_url\":\"https://cf-media.sndcdn.com/4HPwpNqjo7Jh.128.mp3\"")
                    .append("}").toString();

    /**
     * Object to be tested.
     */
    private SoundCloudExtract extract;

    /**
     * Setup {@link #extract}.
     */
    @Before
    public void setup() {
        this.extract = new SoundCloudExtract();

        this.setUp(this.extract, SONG_JSON_REPRESENTATION);
    }

    @Test
    @SneakyThrows
    public void testRetrieveSong() {
        final SoundCloudTrack song = this.extract.extract(COOL_SONG);
        assertNotNull(song.getTitle());
        assertNotNull(song.getArtworkUrl());
        assertNotNull(song.getId());
    }

    @Test
    public void testExtractInvalidUrl() {
        try {
            this.extract.extract("bogus");
            fail();
        } catch (final SoundCloudException ignored) {
            // Expected exception.
        }
    }

    @SneakyThrows
    @Test(timeout = 10000)
    public void integrationTest() {
        final ListeningScheduledExecutorService executorService = MoreExecutors
                .listeningDecorator(Executors.newScheduledThreadPool(4));
        final List<ListenableFuture<?>> futures = Lists.newArrayList();

        for (int i = 0; i < 25; i++) {
            futures.add(executorService.submit(this::testRetrieveSong));
        }

        Futures.allAsList(futures).get();
        executorService.shutdownNow();
    }

}
