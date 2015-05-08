package me.moodcat.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import me.moodcat.database.controllers.ArtistDAO;
import me.moodcat.database.entities.Artist;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The {@code ArtistAPI} is an API entry point to do CRUD operations to {@link Artist} entities.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Path("/api/artists")
@Produces(MediaType.APPLICATION_JSON)
public class ArtistAPI {

    private final ArtistDAO artistDAO;

    @Inject
    @VisibleForTesting
    public ArtistAPI(final ArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }

    @GET
    @Transactional
    public List<Artist> getArtists() {
        return this.artistDAO.listArtists();
    }

    @GET
    @Path("{id}")
    @Transactional
    public Artist getSongById(@PathParam("id") final int id) {
        return this.artistDAO.findById(id);
    }

}
