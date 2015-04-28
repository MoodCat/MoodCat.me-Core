package me.moodcat.soundcloud;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by jaap on 4/28/15.
 */
public class SoundCloudExtractTest {
    final static String COOL_SONG = "https://soundcloud.com/pegboardnerds/who-the-fuck-is-paul-mccartney-kanye-west-x-jennifer-lawrence-x-pegboard-nerds";
    final static String SONG2_ARTIST = "katfyr";
    final static String SONG2_TITLE_ID = "binary-original-mix";
    final static String SONG2_TITLE = "Binary (Original Mix)";
    final static String SONG2_INFO_URL = "https://api.soundcloud.com/resolve.json?url=https://soundcloud.com/katfyr/binary-original-mix&client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

    @Test
    public void testRetrieveSong() throws Exception {
        SoundCloudTrack song = new SoundCloudExtract().extract(COOL_SONG);
        assertNotNull(song.getTitle());
        assertNotNull(song.getArtworkUrl());
        assertNotNull(song.getId());
    }

    @Test
    public void testParseStreamUrl() throws Exception {
        SoundCloudExtract extract = new SoundCloudExtract();
        SoundCloudTrack song = extract.extract(COOL_SONG);
        String mediaUrl = extract.parseStreamUrl(song);
        assertNotNull(mediaUrl);
    }

    @Test
    public void testResolveUrl() throws Exception {
        SoundCloudExtract extract = new SoundCloudExtract();
        String url = extract.resolveUrl(SONG2_ARTIST, SONG2_TITLE_ID);
        SoundCloudTrack track = extract.parseInfoJson(url);
        assertEquals(track.getTitle(), SONG2_TITLE);
    }

    @Test
    public void testParseInfoJson() throws Exception {
        SoundCloudExtract extract = new SoundCloudExtract();
        SoundCloudTrack track = extract.parseInfoJson(SONG2_INFO_URL);
        assertEquals(track.getTitle(), SONG2_TITLE);
    }
}