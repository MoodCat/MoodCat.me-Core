package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * This ExceptionMapper maps {@link NotFoundException NotFoundExceptions} in such a way that the
 * client
 * receives a descriptive JSON response and HTTP status code.
 */
@javax.ws.rs.ext.Provider
public class NotAuthorizedExceptionMapper extends AbstractExceptionMapper<NotAuthorizedException> {

    @Override
    public Response.Status getStatusCode() {
        return FORBIDDEN;
    }

}
