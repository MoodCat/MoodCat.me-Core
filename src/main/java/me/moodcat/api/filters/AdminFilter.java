package me.moodcat.api.filters;

import java.io.IOException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

import me.moodcat.core.mappers.ForbiddenExceptionMapper;
import me.moodcat.database.entities.users.User;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

@javax.ws.rs.ext.Provider
@AdminFiltered
public class AdminFilter implements ContainerResponseFilter {

    private final Provider<User> currentUserProvider;
    
    private final ForbiddenExceptionMapper forbiddenExceptionMapper;

    @Inject
    public AdminFilter(@Named("current.user") final Provider<User> currentUserProvider,
            final ForbiddenExceptionMapper forbiddenExceptionMapper) {
        this.currentUserProvider = currentUserProvider;
        this.forbiddenExceptionMapper = forbiddenExceptionMapper;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
        throws IOException {

        try {
            this.currentUserProvider.get().getAdminStatus().performIfAllowed();
        } catch (final ForbiddenException exception) {
            final Response response = forbiddenExceptionMapper.toResponse(exception);
            requestContext.abortWith(response);
        }
    }

}
