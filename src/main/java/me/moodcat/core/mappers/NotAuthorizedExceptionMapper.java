package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * This ExceptionMapper maps {@link javax.ws.rs.NotFoundException NotFoundExceptions} in such a way that the
 * client
 * receives a descriptive JSON response and HTTP status code.
 */
@Provider
public class NotAuthorizedExceptionMapper extends AbstractExceptionMapper<NotAuthorizedException> {

    @Override
    public Response.Status getStatusCode() {
        return FORBIDDEN;
    }

}
