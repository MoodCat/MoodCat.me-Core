package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;

/**
 * This ExceptionMapper maps {@link EntityNotFoundException EntityNotFoundExceptions} in such a way
 * that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@javax.ws.rs.ext.Provider
public class EntityNotFoundExceptionMapper
        extends AbstractExceptionMapper<EntityNotFoundException> {

    @Override
    public Response.Status getStatusCode() {
        return NOT_FOUND;
    }

}
