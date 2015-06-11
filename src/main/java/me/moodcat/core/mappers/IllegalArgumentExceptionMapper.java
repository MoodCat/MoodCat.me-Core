package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * This ExceptionMapper maps {@link IllegalArgumentException IllegalArgumentExceptions} in such a
 * way that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@Provider
public class IllegalArgumentExceptionMapper extends
        AbstractExceptionMapper<IllegalArgumentException> {

    @Override
    public Response.Status getStatusCode() {
        return BAD_REQUEST;
    }

}
