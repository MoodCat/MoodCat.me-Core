package me.moodcat.api.models;

import junitx.extensions.EqualsHashCodeTestCase;

/**
 * @author Jaap Heijligers
 */
public class SongModelTest extends EqualsHashCodeTestCase {

    public SongModelTest(String name) {
        super(name);
    }

    @Override
    protected SongModel createInstance() throws Exception {
        SongModel model = new SongModel();
        model.setName("First room");
        return model;
    }

    @Override
    protected SongModel createNotEqualInstance() throws Exception {
        SongModel model = new SongModel();
        model.setName("Second room");
        return model;
    }

}
