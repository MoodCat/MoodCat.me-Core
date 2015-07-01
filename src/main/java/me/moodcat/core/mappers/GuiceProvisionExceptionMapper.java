package me.moodcat.core.mappers;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.ProvisionException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * This ExceptionMapper maps {@link ProvisionException ProvisionExceptions} in
 * such a way that the client receives a descriptive JSON response and HTTP
 * status code.
 */
@Provider
@Slf4j
public class GuiceProvisionExceptionMapper extends
        AbstractExceptionMapper<ProvisionException> {

    private final Map<Class<? extends Exception>, AbstractExceptionMapper<? extends Exception>> mappers;

    @Inject
    public GuiceProvisionExceptionMapper(final NotAuthorizedExceptionMapper notAuthorizedExceptionMapper) {
        mappers = Maps.newHashMap();
        
        mappers.put(NotAuthorizedException.class, notAuthorizedExceptionMapper);
    }

    @Override
    protected Response createResponse(final Throwable exception, final UUID id) {
        Throwable cause = exception.getCause();
        
        if (cause != null) {
            return mappers.get(cause.getClass()).createResponse(cause, id);
        }
        
        return super.createResponse(exception, id);
    }
    
    @Override
    protected void logException(final Throwable exception, final UUID id) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        log.error(String.format("[%s] %s (%s)", cause.getClass().getName(), cause.getMessage(), id));
    }

    @Override
    public Status getStatusCode() {
        return Status.INTERNAL_SERVER_ERROR;
    }

}
