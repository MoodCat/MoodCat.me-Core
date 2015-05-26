package me.moodcat.core.mappers;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * This ExceptionMapper maps {@link Throwable Throwables} in such a way that the client
 * receives a descriptive JSON response and HTTP status code.
 */
@javax.ws.rs.ext.Provider
public class ThrowableMapper extends AbstractExceptionMapper<Throwable> {

	@Override
	public Response.Status getStatusCode() {
		return INTERNAL_SERVER_ERROR;
	}
	
}
