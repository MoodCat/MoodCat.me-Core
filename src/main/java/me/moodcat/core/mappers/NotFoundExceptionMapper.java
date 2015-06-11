package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * This ExceptionMapper maps {@link NotFoundException NotFoundExceptions} in such a way that the
 * client
 * receives a descriptive JSON response and HTTP status code.
 */
@Provider
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {

    @Override
    public Response.Status getStatusCode() {
        return NOT_FOUND;
    }

}
