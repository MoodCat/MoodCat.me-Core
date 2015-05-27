package me.moodcat.core.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Test;

/**
 * @author Jaap Heijligers
 */
public class ThrowableMapperTest {

    @Test
    public void testGetStatusCode() throws Exception {
        ThrowableMapper mapper = new ThrowableMapper();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, mapper.getStatusCode());
    }
}
