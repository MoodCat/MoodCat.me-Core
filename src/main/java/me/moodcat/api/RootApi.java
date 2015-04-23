package me.moodcat.api;

import me.moodcat.models.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class RootApi {

    private final static Logger log = LoggerFactory.getLogger(RootApi.class);

    @GET
    public BasicResponse getHelloWorld() {
        BasicResponse response = new BasicResponse();
        response.setaBoolean(true);
        response.setNumber(6);
        response.setTest("Hello World!");
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void postHelloWorld(BasicResponse input) {
        log.info("The input was {}", input);
    }

}
