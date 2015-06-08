package me.moodcat.api.models;

import junitx.extensions.EqualsHashCodeTestCase;

/**
 * @author Jaap Heijligers
 */
public class RoomModelTest extends EqualsHashCodeTestCase {

    public RoomModelTest(String name) {
        super(name);
    }

    private NowPlaying nowPlaying = new NowPlaying();

    @Override
    protected RoomModel createInstance() throws Exception {
        RoomModel model = new RoomModel();
        model.setName("First room");
        model.setNowPlaying(nowPlaying);
        return model;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        RoomModel model = new RoomModel();
        model.setName("Second room");
        model.setNowPlaying(nowPlaying);
        return model;
    }
}
