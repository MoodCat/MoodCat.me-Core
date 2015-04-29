package me.moodcat.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("/api/artists")
@Produces(MediaType.APPLICATION_JSON)
public class ArtistAPI {

    private final ArtistDAO artistDAO;

    @Inject
    @VisibleForTesting
    public ArtistAPI(ArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }

    @GET
    @Transactional
    public List<Artist> getArtists() {
        return artistDAO.listArtists();
    }

    @GET
    @Path("{id}")
    @Transactional
    public Artist getSongById(@PathParam("id") int id) {
        return artistDAO.findById(id);
    }

}
