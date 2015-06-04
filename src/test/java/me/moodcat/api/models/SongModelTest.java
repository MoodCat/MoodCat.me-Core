package me.moodcat.api.models;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import junitx.extensions.EqualsHashCodeTestCase;
import me.moodcat.database.entities.Song;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(Enclosed.class)
public class SongModelTest {

    public static class EqualsSongModelTest extends EqualsHashCodeTestCase {

        public EqualsSongModelTest(final String name) {
            super(name);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected SongModel createInstance() throws Exception {
            final SongModel model = new SongModel();
            model.setName("First room");
            return model;
        }

        @Override
        protected SongModel createNotEqualInstance() throws Exception {
            final SongModel model = new SongModel();
            model.setName("Second room");
            return model;
        }
    }

    public static class TransformSongModelTest {

        private Song song;

        public TransformSongModelTest() {
            song = Mockito.mock(Song.class);
        }

        @Test
        public void canTransformFromSong() {
            assertNotNull(SongModel.transform(song));
        }

        @Test
        public void transformReturnsNullWhenSongIsNull() {
            assertNull(SongModel.transform(null));
        }
    }

}
