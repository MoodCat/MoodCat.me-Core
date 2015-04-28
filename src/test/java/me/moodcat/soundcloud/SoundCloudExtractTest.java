package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

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
    public void testRetrieveSong() throws IOException, SoundCloudException {
        final SoundCloudTrack song = this.extract.extract(COOL_SONG);

        assertNotNull(song.getTitle());
        assertNotNull(song.getArtworkUrl());
        assertNotNull(song.getId());
    }

    @Test
    public void testParseStreamUrl() throws IOException, SoundCloudException {
        final SoundCloudTrack song = this.extract.extract(COOL_SONG);

        // Overwrite song representation to show stream representation.
        Mockito.when(this.factory.getContent(Matchers.anyString())).thenReturn(
                STREAM_JSON_REPRESENTATION);

        final String mediaUrl = this.extract.parseStreamUrl(song);

        assertNotNull(mediaUrl);
    }

    @Test
    public void testResolveUrl() throws IOException {
        final String url = this.extract.resolveUrl(SONG2_ARTIST, SONG2_TITLE_ID);
        final SoundCloudTrack track = this.extract.parseInfoJson(url);

        assertEquals(track.getTitle(), SONG2_TITLE);
    }

    @Test
    public void testParseInfoJson() throws IOException {
        final SoundCloudTrack track = this.extract.parseInfoJson(SONG2_INFO_URL);

        assertEquals(track.getTitle(), SONG2_TITLE);
    }
}
