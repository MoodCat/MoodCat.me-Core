package me.moodcat.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The API to retrieve the moods.
 */
@Path("/api/moods/")
@Produces(MediaType.APPLICATION_JSON)
public class MoodAPI {

    /**
     * Get all the moods defined.
     * 
     * @return The moods defined.
     */
    @GET
    public Mood[] getMoods() {
        return Mood.values();
    }

}
