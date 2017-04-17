package endtoend;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import me.moodcat.api.models.ExceptionResponse;

import org.junit.Test;

public class ExceptionHandlingEndToEndTest extends EndToEndTest {

    @Test
    public void error404IsCorrectlyHandled() {
        Response response = this.perform(invocation -> invocation.path("bogus").request().get());
        ExceptionResponse exceptionResponse = response.readEntity(ExceptionResponse.class);
        assertEquals("Could not find resource for full path: http://localhost:8080/api/bogus",
                exceptionResponse.getMessage());
        assertEquals(404, response.getStatus());
    }

    @Test
    public void error403IsCorrectlyHandled() {
        Response response = this.perform(invocation -> invocation.path("users").path("me")
                .request()
                .get());
        ExceptionResponse exceptionResponse = response.readEntity(ExceptionResponse.class);
        assertEquals("HTTP 401 Unauthorized", exceptionResponse.getMessage());
        assertEquals(403, response.getStatus());
    }

}
