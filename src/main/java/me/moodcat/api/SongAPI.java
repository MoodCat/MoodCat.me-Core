package me.moodcat.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.moodcat.api.filters.AwardPoints;
import me.moodcat.api.models.SongModel;
import me.moodcat.database.controllers.ClassificationDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Classification;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

/**
 * The {@code ArtistAPI} is an API entry point to do CRUD operations to {@link Song} entities.
 */
@Path("/api/songs")
@Produces(MediaType.APPLICATION_JSON)
public class SongAPI {

    /**
     * The points a user gains when he classifies a song.
     */
    protected static final int CLASSIFICATION_POINTS_AWARD = 6;

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
     * Java facade to talk to the database to obtain songs.
     */
    private final SongDAO songDAO;

    private final ClassificationDAO classificationDAO;

    private final Provider<User> currentUserProvider;

    @Inject
    @VisibleForTesting
    public SongAPI(final SongDAO songDAO, final ClassificationDAO classificationDAO,
            @Named("current.user") final Provider<User> currentUserProvider) {
        this.songDAO = songDAO;
        this.classificationDAO = classificationDAO;
        this.currentUserProvider = currentUserProvider;
    }

    @GET
    @Transactional
    public List<SongModel> getSongs() {
        return transformSongs(songDAO.listSongs());
    }

    @GET
    @Path("{id}")
    @Transactional
    public SongModel getSongById(@PathParam("id") final int id) {
        return SongModel.transform(songDAO.findById(id));
    }

    @GET
    @Path("toclassify")
    @Transactional
    public List<SongModel> toClassify() {
        return transformSongs(songDAO.listRandomsongs(NUMBER_OF_CLASSIFICATION_SONGS));
    }

    @GET
    @Path("query-range")
    @Transactional
    public List<SongModel> queryRange(@QueryParam("valence") final double valence,
            @QueryParam("arousal") final double arousal) {
        final VAVector vector = new VAVector(valence, arousal);
        return transformSongs(songDAO.findForDistance(vector, 2));
    }

    private List<SongModel> transformSongs(final List<Song> songs) {
        return songs.stream()
                .map(SongModel::transform)
                .collect(Collectors.toList());
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
    @AwardPoints(CLASSIFICATION_POINTS_AWARD)
    public ClassificationRequest classifySong(@PathParam("id") final int id,
            final ClassificationRequest classification)
            throws InvalidClassificationException {

        final User user = this.currentUserProvider.get();
        final Song song = this.songDAO.findBySoundCloudId(id);
        final VAVector classificationVector = new VAVector(
                classification.getValence(),
                classification.getArousal());

        if (classificationDAO.exists(user, song)) {
            /*
             * TODO This should not happen, because after the initial classification
             * the song will not be available for classification (and this endpoint
             * should throw an IllegalArgumentException instead). However, this
             * endpoint is currently also abused for the classification game.
             * In order to prevent users to abuse this to gain points with fake
             * classifications on the same song, we ignore duplicate classifications.
             */
            throw new IllegalArgumentException("Already classified this song");
        }

        assertDimensionIsValid(classification.getValence());
        assertDimensionIsValid(classification.getArousal());

        final VAVector vector;
        if (song.getValenceArousal().distance(VAVector.ZERO) < 1e-2) {
            // If near origin set the vector
            vector = classificationVector;
        } else {
            // otherwise adjust the vector
            vector = adjustSongVector(classificationVector, song);
        }

        song.setValenceArousal(vector);
        this.persistClassification(user, song, classificationVector);
        this.songDAO.merge(song);

        return classification;
    }

    private void persistClassification(final User user, final Song song,
            final VAVector classificationVector) {
        final Classification classificationEntity = new Classification();
        classificationEntity.setValenceArousal(classificationVector);
        classificationEntity.setSong(song);
        classificationEntity.setUser(user);
        this.classificationDAO.persist(classificationEntity);
    }

    /**
     * Process a user classification game for the given songId.
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
    @Path("{id}/classifygame")
    @Transactional
    @AwardPoints(CLASSIFICATION_POINTS_AWARD)
    public ClassificationRequest approachSong(@PathParam("id") final int id,
            final ClassificationRequest classification)
            throws InvalidClassificationException {
        final Song song = this.songDAO.findBySoundCloudId(id);

        assertDimensionIsValid(classification.getValence());
        assertDimensionIsValid(classification.getArousal());

        song.setValenceArousal(new VAVector(classification.getValence(), classification
                .getArousal()));
        songDAO.merge(song);

        return classification;
    }

    private void assertDimensionIsValid(final double value) throws InvalidClassificationException {
        if (!Arrays.asList(ACCEPTED_DIMENSION_VALUES).contains(value)) {
            throw new InvalidClassificationException();
        }
    }

    private VAVector adjustSongVector(final VAVector classificationVector, final Song song) {
        final VAVector songVector = song.getValenceArousal();

        final VAVector scaledDistance = classificationVector.subtract(songVector)
                .multiply(CLASSIFICATION_WEIGHT);

        return songVector.add(scaledDistance);
    }

    /**
     * ClassificationRequest with the arousal and valence the user would classify the specific song
     * for.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClassificationRequest {

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

}
