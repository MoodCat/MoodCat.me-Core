package me.moodcat.core.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author Jaap Heijligers
 */
public class NotFoundExceptionMapperTest {

    private NotFoundExceptionMapper mapper;

    @Mock
    private NotFoundException exception;

    @Before
    public void before() {
        mapper = new NotFoundExceptionMapper();
        exception = Mockito.mock(NotFoundException.class);
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(mapper.getStatusCode(), Response.Status.NOT_FOUND);
    }

    @Test
    public void testToResponse() {
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), exception.getMessage());
    }

    @Test
    public void testToResponseMessage() {
        final String message = "Test Message";
        Mockito.when(exception.getMessage()).thenReturn(message);
        Response response = mapper.toResponse(exception);
        AbstractExceptionMapper.ExceptionResponse responseEntity = (AbstractExceptionMapper.ExceptionResponse) response
                .getEntity();
        assertEquals(responseEntity.getMessage(), message);
    }
}
