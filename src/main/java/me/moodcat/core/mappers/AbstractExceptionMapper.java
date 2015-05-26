package me.moodcat.core.mappers;

import java.util.UUID;

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
    public Response toResponse(final T exception) {
        final UUID id = UUID.randomUUID();
        log.error(exception.getMessage() + " (" + id + ")", exception);

        final ExceptionResponse exceptionResponse = createResponse(exception);
        exceptionResponse.setUuid(id.toString());

        return Response.status(getStatusCode())
                .entity(exceptionResponse)
                .build();
    }

    protected ExceptionResponse createResponse(final T exception) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(exception.getMessage());
        return exceptionResponse;
    }

    public abstract Status getStatusCode();

    /**
     * The response to the frontend.
     */
    protected static class ExceptionResponse {

        /**
         * The unique id for the exception.
         */
        @Getter
        @Setter
        private String uuid;

        /**
         * The exception message.
         */
        @Getter
        @Setter
        private String message;

    }

}
