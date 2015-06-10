package me.moodcat.core.mappers;

import com.google.inject.Inject;
import com.google.inject.ProvisionException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

/**
 * This ExceptionMapper maps {@link ProvisionException ProvisionExceptions} in such a way
 * that the client receives a descriptive JSON response and HTTP status code.
 */
@Provider
public class GuiceProvisionExceptionMapper extends AbstractExceptionMapper<ProvisionException> {

    private final NotAuthorizedExceptionMapper notAuthorizedExceptionMapper;

    @Inject
    public GuiceProvisionExceptionMapper(final NotAuthorizedExceptionMapper notAuthorizedExceptionMapper) {
        this.notAuthorizedExceptionMapper = notAuthorizedExceptionMapper;
    }

    @Override
    protected Response createResponse(final ProvisionException exception, final UUID id) {
        Throwable cause = exception;
        while(cause != null) {
            if(cause instanceof NotAuthorizedException) {
                return notAuthorizedExceptionMapper.createResponse((NotAuthorizedException) cause, id);
            }
            cause = cause.getCause();
        }
        return super.createResponse(exception, id);
    }

    @Override
    public Status getStatusCode() {
        return Status.INTERNAL_SERVER_ERROR;
    }

}
