package me.moodcat.core.mappers;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jaap Heijligers
 */
public class EntityNotFoundExceptionMapperTest {

    private EntityNotFoundExceptionMapper mapper;

    @Before
    public void before() {
        mapper = new EntityNotFoundExceptionMapper();
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(mapper.getStatusCode(), Response.Status.NOT_FOUND);
    }

    @Test
    public void testToResponse() throws Exception {
        EntityNotFoundException exception = new EntityNotFoundException();
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), exception.getMessage());
    }

    @Test
    public void testToResponseMessage() throws Exception {
        final String message = "Test Message";
        EntityNotFoundException exception = new EntityNotFoundException(message);
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), message);
    }
}
