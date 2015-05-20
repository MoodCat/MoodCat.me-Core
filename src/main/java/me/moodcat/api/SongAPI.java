package me.moodcat.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.Data;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The {@code ArtistAPI} is an API entry point to do CRUD operations to {@link Song} entities.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Path("/api/songs")
@Produces(MediaType.APPLICATION_JSON)
public class SongAPI {

    /**
     * Used to deter users of voting to already widely accepted songs.
     */
    protected static final int MINIMUM_NUMBER_OF_POSITIVE_VOTES = 5;

    /**
     * The weight used to modify {@link VAVector vectors} of {@link Song songs} with a
     * classification vector.
     */
    private static final double CLASSIFICATION_WEIGHT = 0.01;

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

    /**
     * Process a user classification for the given songId.
     *
     * @param id
     *            The id of the song.
     * @param classification
     *            The classification of the user for the song.
     * @return The classification provided.
     */
    @POST
    @Path("{id}/classify")
    @Transactional
    public ClassificationRequest classifySong(@PathParam("id") final int id,
            final ClassificationRequest classification) {
        final Song song = this.songDAO.findById(id);

        if (song.getNumberOfPositiveVotes() < MINIMUM_NUMBER_OF_POSITIVE_VOTES) {
            final VAVector classificationVector = new VAVector(

                    classification.getValence(),
                    classification.getArousal());

            final VAVector songVector = song.getValenceArousal();

            final VAVector scaledDistance = songVector.subtract(classificationVector)
                    .multiply(CLASSIFICATION_WEIGHT);

            song.setValenceArousal(songVector.add(scaledDistance));
            this.songDAO.merge(song);
        }

        return classification;
    }

    /**
     * Process a vote to a song. A vote is either "like" or "dislike".
     *
     * @param id
     *            The songID.
     * @param vote
     *            The vote.
     * @return The song object, if the process was succesful.
     * @throws InvalidVoteException
     *             Thrown if an invalid vote was cast.
     */
    @POST
    @Path("{id}/vote/{vote}")
    @Transactional
    public Song voteSong(@PathParam("id") final int scid,
            @PathParam("vote") final String vote) throws InvalidVoteException {
        final Song song = this.songDAO.findBySoundCloudId(scid);

        if (vote.equals("like")) {
            song.increaseNumberOfPositiveVotes();
        } else if (vote.equals("dislike")) {
            song.decreaseNumberOfPositiveVotes();
        } else {
            throw new InvalidVoteException();
        }

        return song;
    }

    /**
     * Classificationrequest with the arousal and valence the user would classify the specific song
     * for.
     *
     * @author JeremybellEU
     */
    @Data
    protected static class ClassificationRequest {

        /**
         * The arousal for the song.
         */
        private final double arousal;

        /**
         * The valence for the song.
         */
        private final double valence;
    }

    /**
     * Thrown if the vote was invalid.
     *
     * @author JeremybellEU
     */
    protected static class InvalidVoteException extends Exception {

        /**
         * Generated ID.
         */
        private static final long serialVersionUID = -7422916305683573138L;
    }

}
