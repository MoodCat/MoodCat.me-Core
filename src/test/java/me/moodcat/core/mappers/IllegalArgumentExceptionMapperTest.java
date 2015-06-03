package me.moodcat.core.mappers;

import javax.ws.rs.core.Response.Status;

/**
 * @author Jaap Heijligers
 */
public class IllegalArgumentExceptionMapperTest extends
        ExceptionMapperTest<IllegalArgumentException> {

    public IllegalArgumentExceptionMapperTest() {
        super(new IllegalArgumentExceptionMapper());
    }

    @Override
    protected Status getResponseStatus() {
        return Status.BAD_REQUEST;
    }
}
