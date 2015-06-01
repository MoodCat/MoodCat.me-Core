package me.moodcat.core.mappers;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response.Status;

/**
 * @author Jaap Heijligers
 */
public class EntityNotFoundExceptionMapperTest extends ExceptionMapperTest<EntityNotFoundException> {

    public EntityNotFoundExceptionMapperTest() {
        super(new EntityNotFoundExceptionMapper());
    }

    @Override
    protected Status getResponseStatus() {
        return Status.NOT_FOUND;
    }
}
