package me.moodcat.core.mappers;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

/**
 * This ExceptionMapper maps {@link Throwable Throwables} in such a way that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@Provider
@Slf4j
public class ThrowableMapper extends AbstractExceptionMapper<Throwable> {

    @Override
    public Response.Status getStatusCode() {
        return INTERNAL_SERVER_ERROR;
    }
    
    /**
     * Log the full stacktrace.
     */
    @Override
    protected void logException(final Throwable exception, final UUID id) {
        log.error(String.format("%s (%s)", exception.getMessage(), id), exception);
    }

}
