package me.moodcat.core.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import me.moodcat.api.models.ExceptionResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ExceptionMapperTest<T extends Throwable> {

    protected AbstractExceptionMapper<T> mapper;

    @Mock
    protected Throwable exception;

    protected ExceptionMapperTest(final AbstractExceptionMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(mapper.getStatusCode(), this.getResponseStatus());
    }

    protected abstract Response.Status getResponseStatus();

    @Test
    public void testToResponse() throws Exception {
        final Response response = mapper.toResponse(exception);
        final ExceptionResponse responseEntity = (ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), exception.getMessage());
    }

    @Test
    public void testToResponseMessage() throws Exception {
        final String message = "Test Message";
        Mockito.when(exception.getMessage()).thenReturn(message);
        final Response response = mapper.toResponse(exception);
        final ExceptionResponse responseEntity = (ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), message);
    }

}
