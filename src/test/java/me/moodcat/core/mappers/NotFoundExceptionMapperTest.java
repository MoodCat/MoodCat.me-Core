package me.moodcat.core.mappers;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response.Status;

/**
 * @author Jaap Heijligers
 */
public class NotFoundExceptionMapperTest extends ExceptionMapperTest<NotFoundException> {

    public NotFoundExceptionMapperTest() {
        super(new NotFoundExceptionMapper());
    }

    @Override
    protected Status getResponseStatus() {
        return Status.NOT_FOUND;
    }
}
