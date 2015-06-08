package me.moodcat.api;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The {@code ArtistAPI} is an API entry point to do CRUD operations to {@link Song} entities.
 */
@Path("/api/songs")
@Produces(MediaType.APPLICATION_JSON)
public class SongAPI {

    /**
     * Used to deter users of voting to already widely accepted songs.
     */
    protected static final int MINIMUM_NUMBER_OF_POSITIVE_VOTES = 5;

    /**
     * The number of songs retrieved for each classification list.
     */
    private static final int NUMBER_OF_CLASSIFICATION_SONGS = 5;

    /**
     * The weight used to modify {@link VAVector vectors} of {@link Song songs} with a
     * classification vector.
     */
    private static final double CLASSIFICATION_WEIGHT = 0.01;

    /**
     * The accepted valence and arousal values.
     */
    private static final Double[] ACCEPTED_DIMENSION_VALUES = new Double[] {
            -1.0, -0.5, 0.0, 0.5, 1.0
    };

    /**
     * Manager to talk to the database to obtain songs.
     */
    private final SongDAO songDAO;

    @Inject
    @VisibleForTesting
    public SongAPI(final SongDAO songDAO) {
        this.songDAO = songDAO;
    }

    @GET
    @Transactional
    public List<Song> getSongs() {
        return songDAO.listSongs();
    }

    @GET
    @Path("{id}")
    @Transactional
    public Song getSongById(@PathParam("id") final int id) {
        return songDAO.findById(id);
    }

    @GET
    @Path("toclassify")
    @Transactional
    public List<Song> toClassify() {
        return songDAO.listRandomsongs(NUMBER_OF_CLASSIFICATION_SONGS);
    }

    /**
     * Process a user classification for the given songId.
     *
     * @param id
     *            The id of the song.
     * @param classification
     *            The classification of the user for the song.
     * @return The classification provided.
     * @throws InvalidClassificationException
     *             When the classification provided was invalid
     */
    @POST
    @Path("{id}/classify")
    @Transactional
    public ClassificationRequest classifySong(@PathParam("id") final int id,
            final ClassificationRequest classification) throws InvalidClassificationException {
        final Song song = this.songDAO.findBySoundCloudId(id);
        assertDimensionIsValid(classification.getValence());
        assertDimensionIsValid(classification.getArousal());

        if (song.getNumberOfPositiveVotes() < MINIMUM_NUMBER_OF_POSITIVE_VOTES) {
            song.setValenceArousal(adjustSongVector(classification, song));
            this.songDAO.merge(song);
        }

        return classification;
    }

    /**
     * Process a vote to a song. A vote is either "like" or "dislike".
     *
     * @param soundCloudId
     *            The soundCloudID.
     * @param vote
     *            The vote.
     * @return The song object, if the process was succesful.
     * @throws InvalidVoteException
     *             Thrown if an invalid vote was cast.
     */
    @POST
    @Path("{id}/vote/{vote}")
    @Transactional
    public Song voteSong(@PathParam("id") final int soundCloudId,
            @PathParam("vote") final String vote) throws InvalidVoteException {
        final Song song = this.songDAO.findBySoundCloudId(soundCloudId);

        if ("like".equals(vote)) {
            song.increaseNumberOfPositiveVotes();
        } else if ("dislike".equals(vote)) {
            song.decreaseNumberOfPositiveVotes();
        } else {
            throw new InvalidVoteException();
        }

        return song;
    }

    private void assertDimensionIsValid(final double value) throws InvalidClassificationException {
        if (!Arrays.asList(ACCEPTED_DIMENSION_VALUES).contains(value)) {
            throw new InvalidClassificationException();
        }
    }

    private VAVector adjustSongVector(final ClassificationRequest classification, final Song song) {
        final VAVector classificationVector = new VAVector(

                classification.getValence(),
                classification.getArousal());

        final VAVector songVector = song.getValenceArousal();

        final VAVector scaledDistance = classificationVector.subtract(songVector)
                .multiply(CLASSIFICATION_WEIGHT);

        return songVector.add(scaledDistance);
    }

    /**
     * Classificationrequest with the arousal and valence the user would classify the specific song
     * for.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    protected static class ClassificationRequest {

        /**
         * The valence for the song.
         *
         * @param valence
         *            The valence to set.
         * @return The valence that was classified.
         */
        private double valence;

        /**
         * The arousal for the song.
         *
         * @param arousal
         *            The arousal to set.
         * @return The arousal that was classified.
         */
        private double arousal;
    }

    /**
     * Thrown if the classification was invalid.
     */
    protected static class InvalidClassificationException extends IllegalArgumentException {

        /**
         * Generated ID.
         */
        private static final long serialVersionUID = -6684926632173744801L;
    }

    /**
     * Thrown if the vote was invalid.
     */
    protected static class InvalidVoteException extends IllegalArgumentException {

        /**
         * Generated ID.
         */
        private static final long serialVersionUID = -7422916305683573138L;
    }

}
