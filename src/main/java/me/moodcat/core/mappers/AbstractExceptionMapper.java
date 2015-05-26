package me.moodcat.core.mappers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    protected static class ExceptionResponse {

        @Getter
        @Setter
        private String uuid;

        @Getter
        @Setter
        private String message;

    }

    @Override
    public Response toResponse(final T exception) {
        final UUID id = UUID.randomUUID();
        log.error(exception.getMessage() + " (" + id + ")", exception);

        ExceptionResponse exceptionResponse = createResponse(exception);
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

}
