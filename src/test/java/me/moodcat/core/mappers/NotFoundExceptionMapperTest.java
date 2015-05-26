package me.moodcat.core.mappers;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

/**
 * @author Jaap Heijligers
 */
public class NotFoundExceptionMapperTest {
    private NotFoundExceptionMapper mapper;

    @Before
    public void before() {
        mapper = new NotFoundExceptionMapper();
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(mapper.getStatusCode(), Response.Status.NOT_FOUND);
    }

    @Test
    public void testToResponse() throws Exception {
        NotFoundException exception = new NotFoundException();
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), exception.getMessage());
    }

    @Test
    public void testToResponseMessage() throws Exception {
        final String message = "Test Message";
        NotFoundException exception = new NotFoundException(message);
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), message);
    }
}
