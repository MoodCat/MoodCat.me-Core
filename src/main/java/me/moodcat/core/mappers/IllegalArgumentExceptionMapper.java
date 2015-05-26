package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;

/**
 * This ExceptionMapper maps {@link IllegalArgumentException IllegalArgumentExceptions} in such a
 * way that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@javax.ws.rs.ext.Provider
public class IllegalArgumentExceptionMapper extends
        AbstractExceptionMapper<IllegalArgumentException> {

    @Override
    public Response.Status getStatusCode() {
        return BAD_REQUEST;
    }

}
