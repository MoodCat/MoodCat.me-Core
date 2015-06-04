package me.moodcat.api.models;

import junitx.extensions.EqualsHashCodeTestCase;

/**
 * @author Jaap Heijligers
 */
public class RoomModelTest extends EqualsHashCodeTestCase {

    public RoomModelTest(String name) {
        super(name);
    }

    private SongModel song = new SongModel();

    @Override
    protected RoomModel createInstance() throws Exception {
        RoomModel model = new RoomModel();
        model.setTime(0);
        model.setSong(song);
        model.setName("First room");
        return model;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        RoomModel model = new RoomModel();
        model.setTime(0);
        model.setSong(song);
        model.setName("Second room");
        return model;
    }
}
