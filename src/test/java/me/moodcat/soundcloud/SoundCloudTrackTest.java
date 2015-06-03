package me.moodcat.soundcloud;

import junitx.extensions.EqualsHashCodeTestCase;
import me.moodcat.soundcloud.SoundCloudTrack.User;

public class SoundCloudTrackTest extends EqualsHashCodeTestCase {

    public SoundCloudTrackTest(final String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        final SoundCloudTrack track = new SoundCloudTrack();
        track.setArtworkUrl("artworkurl");
        track.setDownloadable(false);
        track.setDuration(100000);
        track.setId(1);
        track.setPermalink("permalink");
        track.setTitle("Title");

        final User user = new User();
        user.setUsername("artist1");
        track.setUser(user);

        return track;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        final SoundCloudTrack track = new SoundCloudTrack();
        track.setArtworkUrl("artworkurl");
        track.setDownloadable(false);
        track.setDuration(100000);
        track.setId(1);
        track.setPermalink("permalink");
        track.setTitle("Title");

        final User user = new User();
        user.setUsername("artist2");
        track.setUser(user);

        return track;
    }

}
