package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import me.moodcat.soundcloud.SoundCloudExtract.HttpClientInvoker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by jaap on 4/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class SoundCloudExtractTest {

    /**
     * We like this song.
     */
    private static final String COOL_SONG = "https://soundcloud.com/pegboardnerds/"
            + "who-the-fuck-is-paul-mccartney-kanye-west-x-jennifer-lawrence-x-pegboard-nerds";

    /**
     * Returns JSON-representations.
     */
    @Mock
    protected HttpClientInvoker factory;

    /**
     * Object to be tested.
     */
    private SoundCloudExtract extract;

    @Mock
    private SoundCloudTrack track;

    /**
     * Setup {@link #extract}.
     *
     * @throws SoundCloudException
     */
    @Before
    public void setup() throws SoundCloudException {
        this.extract = new SoundCloudExtract();
        this.extract.setUrlFactory(this.factory);

        Mockito.when(this.factory.resolve(Matchers.anyString(),
                Matchers.<Class<SoundCloudTrack>> any()))
                .thenReturn(this.track);
    }

    /**
     * Extract a song and verify it is the track we want to retrieve.
     *
     * @throws SoundCloudException
     */
    @Test
    public void testRetrieveSong() throws SoundCloudException {
        final SoundCloudTrack song = this.extract.extract(COOL_SONG);

        assertEquals(this.track, song);
    }

    /**
     * Verify that if the url is not a correct soundcloud api call, a {@link SoundCloudException} is
     * thrown.
     *
     * @throws SoundCloudException
     */
    @Test(expected = SoundCloudException.class)
    public void testExtractInvalidUrl() throws SoundCloudException {
        this.extract.extract("bogus");
    }

}
