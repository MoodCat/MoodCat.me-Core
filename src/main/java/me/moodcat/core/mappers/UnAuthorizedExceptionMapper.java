package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import me.moodcat.core.UnauthorizedException;

/**
 * This ExceptionMapper maps {@link IllegalArgumentException IllegalArgumentExceptions} in such a
 * way that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@Provider
public class UnAuthorizedExceptionMapper extends AbstractExceptionMapper<UnauthorizedException> {

    @Override
    public Response.Status getStatusCode() {
        return FORBIDDEN;
    }

}
