package me.moodcat.api;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import me.moodcat.api.SongAPI.ClassificationRequest;
import me.moodcat.api.SongAPI.InvalidClassificationException;
import me.moodcat.api.SongAPI.InvalidVoteException;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SongAPITest {

    private static final int SONG_ID = 1;

    private static final int SOUNCLOUD_ID = 25;

    private static final int NUMBER_OF_VOTES = 3;

    @Mock
    private SongDAO songDAO;

    @InjectMocks
    private SongAPI songAPI;

    @Mock
    private Song song;

    @Captor
    private ArgumentCaptor<VAVector> vectorCaptor;

    @Before
    public void setUp() {
        when(song.getId()).thenReturn(SONG_ID);
        when(song.getSoundCloudId()).thenReturn(SOUNCLOUD_ID);
        when(song.getNumberOfPositiveVotes()).thenReturn(NUMBER_OF_VOTES);

        when(songDAO.findById(Matchers.eq(SONG_ID))).thenReturn(song);
        when(songDAO.findBySoundCloudId(Matchers.eq(SOUNCLOUD_ID))).thenReturn(song);
    }

    @Test
    public void classificationUpdatesSong() throws InvalidClassificationException {
        Mockito.doNothing().when(song).setValenceArousal(vectorCaptor.capture());
        final VAVector songVector = new VAVector(0.5, 0.5);
        when(song.getValenceArousal()).thenReturn(songVector);

        final ClassificationRequest request = new ClassificationRequest(1.0, 0.0);

        songAPI.classifySong(SONG_ID, request);

        verify(songDAO).merge(song);
        assertTrue(vectorCaptor.getValue().getValence() > 0.5);
        assertTrue(vectorCaptor.getValue().getArousal() < 0.5);
    }

    @Test
    public void classificationDoesNotUpdateSongWhenEnoughPositiveVotes()
            throws InvalidClassificationException {
        when(song.getNumberOfPositiveVotes()).thenReturn(
                SongAPI.MINIMUM_NUMBER_OF_POSITIVE_VOTES + 1);

        final ClassificationRequest request = new ClassificationRequest(0.5, 0.5);

        songAPI.classifySong(SONG_ID, request);

        verify(songDAO, never()).merge(song);
    }

    @Test(expected = InvalidClassificationException.class)
    public void invalidClassificationThrowsException()
            throws InvalidClassificationException {

        final ClassificationRequest request = new ClassificationRequest(0.8, 0.2);

        songAPI.classifySong(SONG_ID, request);
    }

    @Test
    public void positiveVoteIncreasesSongVotes() throws InvalidVoteException {
        songAPI.voteSong(SOUNCLOUD_ID, "like");

        verify(song).increaseNumberOfPositiveVotes();
    }

    @Test
    public void positiveVoteDecreasesSongVotes() throws InvalidVoteException {
        songAPI.voteSong(SOUNCLOUD_ID, "dislike");

        verify(song).decreaseNumberOfPositiveVotes();
    }

    @Test(expected = InvalidVoteException.class)
    public void bogusVoteThrowsException() throws InvalidVoteException {
        songAPI.voteSong(SOUNCLOUD_ID, "bogus");
    }

}
