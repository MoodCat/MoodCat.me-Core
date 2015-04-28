package me.moodcat.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExtractTest {

    private static final String COOL_SONG = "https://soundcloud.com/pegboardnerds/"
            + "who-the-fuck-is-paul-mccartney-kanye-west-x-jennifer-lawrence-x-pegboard-nerds";

    private static final String SONG2_ARTIST = "katfyr";

    private static final String SONG2_TITLE_ID = "binary-original-mix";

    private static final String SONG2_TITLE = "Binary (Original Mix)";

    private static final String SONG2_INFO_URL = "https://api.soundcloud.com/resolve.json"
            + "?url=https://soundcloud.com/katfyr/binary-original-mix&client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

    private SoundCloudExtract extract;

    @Before
    public void setUp() {
        this.extract = new SoundCloudExtract();
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
