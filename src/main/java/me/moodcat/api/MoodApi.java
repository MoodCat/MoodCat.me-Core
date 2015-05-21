package me.moodcat.api;

import lombok.Data;
import me.moodcat.mood.Mood;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("api/mood")
@Produces(MediaType.APPLICATION_JSON)
public class MoodApi {

    @GET
    public MoodResponse getTheMoodzzz() {
        return new MoodResponse();
    }

    @Data
    public static class MoodResponse {

        private final Mood[] moods = Mood.values();

    }

}
