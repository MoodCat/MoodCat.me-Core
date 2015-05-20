package me.moodcat.core.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jaap Heijligers
 */
public class IllegalArgumentExceptionMapperTest {

    private IllegalArgumentExceptionMapper mapper;

    @Before
    public void before() {
        mapper = new IllegalArgumentExceptionMapper();
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(mapper.getStatusCode(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void testToResponse() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException();
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), exception.getMessage());
    }

    @Test
    public void testToResponseMessage() throws Exception {
        final String message = "Test Message";
        IllegalArgumentException exception = new IllegalArgumentException(message);
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), message);
    }
}