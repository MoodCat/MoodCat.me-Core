package me.moodcat.core.mappers;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.ProvisionException;

public class GuiceProvisionExceptionMapperTest extends
        ExceptionMapperTest<ProvisionException> {

    private static final NotAuthorizedExceptionMapper NOT_AUTHORIZED_MAPPER = mock(NotAuthorizedExceptionMapper.class);

    private static final Exception GENERAL_EXCEPTION = mock(Exception.class);

    private static final NotAuthorizedException NOT_AUTHORIZED_EXCEPTION = mock(NotAuthorizedException.class);

    public GuiceProvisionExceptionMapperTest() {
        super(new GuiceProvisionExceptionMapper(NOT_AUTHORIZED_MAPPER));
    }

    @Override
    protected Status getResponseStatus() {
        return Status.INTERNAL_SERVER_ERROR;
    }

    @Test
    public void usesNotAuthorizedMapperWhenCauseIsNotAuthorized() {
        when(this.exception.getCause()).thenReturn(NOT_AUTHORIZED_EXCEPTION);

        UUID randomUuid = UUID.randomUUID();

        this.mapper.createResponse(this.exception, randomUuid);

        Mockito.verify(NOT_AUTHORIZED_MAPPER).createResponse(eq(NOT_AUTHORIZED_EXCEPTION),
                eq(randomUuid));
    }

    @Test
    public void usesNotAuthorizedMapperWhenCascadedCauseIsNotAuthorized() {
        when(this.exception.getCause()).thenReturn(GENERAL_EXCEPTION);
        when(GENERAL_EXCEPTION.getCause()).thenReturn(NOT_AUTHORIZED_EXCEPTION);

        UUID randomUuid = UUID.randomUUID();

        this.mapper.createResponse(this.exception, randomUuid);

        verify(NOT_AUTHORIZED_MAPPER).createResponse(eq(NOT_AUTHORIZED_EXCEPTION), eq(randomUuid));
    }
}
