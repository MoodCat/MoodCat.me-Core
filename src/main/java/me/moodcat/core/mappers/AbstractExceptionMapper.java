package me.moodcat.core.mappers;

import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An ExceptionMapper which can catch exceptions and report to the frontend.
 *
 * @param <T>
 *            The exception to report.
 */
@Slf4j
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(final Throwable exception) {
        final UUID id = UUID.randomUUID();
        this.logException(exception, id);
        return createResponse(exception, id);
    }

    /**
     * Override this method to change the way exceptions are logged on the server.
     * Useful to determine whether to print the full stacktrace or not.
     * By default all explicit ExceptionMappers don't print this, however the
     * {@link ThrowableMapper} does print this.
     * 
     * @param exception
     *            The exception to log.
     * @param id
     *            The id of the exception.
     */
    protected void logException(final Throwable exception, final UUID id) {
        log.error(String.format("[%s] %s (%s)", exception.getClass().getName(), exception.getMessage(), id));
    }

    protected Response createResponse(final Throwable exception, final UUID id) {
        final ExceptionResponse exceptionResponse = createResponse(exception);
        exceptionResponse.setUuid(id.toString());

        return Response.status(getStatusCode())
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(exceptionResponse)
            .build();
    }

    protected ExceptionResponse createResponse(final Throwable exception) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(exception.getMessage());
        return exceptionResponse;
    }

    public abstract Status getStatusCode();

    /**
     * The response to the frontend.
     */
    public static class ExceptionResponse {

        /**
         * The unique id for the exception.
         *
         * @param uuid
         *            The new unique id of this response.
         * @return The unique id of this response.
         */
        @Getter
        @Setter
        private String uuid;

        /**
         * The exception message.
         *
         * @param message
         *            The message to inform the user.
         * @return The message of this response.
         */
        @Getter
        @Setter
        private String message;

    }

}
