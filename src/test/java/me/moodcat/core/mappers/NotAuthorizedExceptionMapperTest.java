package me.moodcat.core.mappers;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response.Status;

public class NotAuthorizedExceptionMapperTest extends ExceptionMapperTest<NotAuthorizedException> {

	    public NotAuthorizedExceptionMapperTest() {
	        super(new NotAuthorizedExceptionMapper());
	    }

	    @Override
	    protected Status getResponseStatus() {
	        return Status.FORBIDDEN;
	    }
	}
