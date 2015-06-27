package me.moodcat.api;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import me.moodcat.api.SongAPI.ClassificationRequest;
import me.moodcat.api.SongAPI.InvalidClassificationException;
import me.moodcat.database.controllers.ClassificationDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class SongAPITest {

    private static final int SONG_ID = 1;

    private static final int SOUNCLOUD_ID = 25;
    
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private SongDAO songDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private Provider<User> currentUserProvider;

    @Mock
    private ClassificationDAO classificationDAO;

    @InjectMocks
    private SongAPI songAPI;

    @Mock
    private Song song;

    @Mock
    private User user;

    @Captor
    private ArgumentCaptor<VAVector> vectorCaptor;

    @Before
    public void setUp() {
        when(song.getId()).thenReturn(SONG_ID);
        when(song.getSoundCloudId()).thenReturn(SOUNCLOUD_ID);

        final VAVector songVector = new VAVector(0.5, 0.5);
        when(song.getValenceArousal()).thenReturn(songVector);

        when(songDAO.findById(Matchers.eq(SONG_ID))).thenReturn(song);
        when(songDAO.findBySoundCloudId(Matchers.eq(SOUNCLOUD_ID))).thenReturn(song);

        when(currentUserProvider.get()).thenReturn(user);
    }

    @Test
    public void classificationUpdatesSong() throws InvalidClassificationException {
        Mockito.doNothing().when(song).setValenceArousal(vectorCaptor.capture());

        final ClassificationRequest request = new ClassificationRequest(1.0, 0.0);

        songAPI.classifySong(SOUNCLOUD_ID, request);

        verify(songDAO).merge(song);
        assertTrue(vectorCaptor.getValue().getValence() > 0.5);
        assertTrue(vectorCaptor.getValue().getArousal() < 0.5);
    }

    @Test(expected = InvalidClassificationException.class)
    public void invalidClassificationThrowsException()
            throws InvalidClassificationException {

        final ClassificationRequest request = new ClassificationRequest(0.8, 0.2);

        songAPI.classifySong(SOUNCLOUD_ID, request);
    }

    @Test
    public void classificationTwiceThrowsException() throws InvalidClassificationException {
        final ClassificationRequest request = new ClassificationRequest(1.0, 1.0);

        songAPI.classifySong(SOUNCLOUD_ID, request);
        
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Already classified");
        
        when(classificationDAO.exists(user, song)).thenReturn(true);
        
        songAPI.classifySong(SOUNCLOUD_ID, request);
    }

    @Test
    public void canRetrieveAllSongs() {
        songAPI.getSongs();

        verify(songDAO).listSongs();
    }

    @Test
    public void canSupplyRandomVectorsForClassification() {
        songAPI.toClassify();

        verify(songDAO).listRandomsongs(anyInt());
    }

    @Test
    public void canRetrieveSongById() {
        songAPI.getSongById(SONG_ID);

        verify(songDAO).findById(SONG_ID);
    }

    @Test
    public void classificationApproachesSong() throws InvalidClassificationException {
        final ClassificationRequest request = new ClassificationRequest(1.0, 0.0);

        songAPI.approachSong(SOUNCLOUD_ID, request);

        verify(songDAO).merge(song);
        verify(song).setValenceArousal(eq(new VAVector(1.0, 0.0)));
    }
    
    @Test
    public void classificationIsSetWhenCloseToZeroVector() throws InvalidClassificationException {
        when(song.getValenceArousal()).thenReturn(VAVector.ZERO);
        
        final ClassificationRequest request = new ClassificationRequest(1.0, 0.0);

        songAPI.classifySong(SOUNCLOUD_ID, request);

        verify(songDAO).merge(song);
        verify(song).setValenceArousal(eq(new VAVector(1.0, 0.0)));
    }

}
