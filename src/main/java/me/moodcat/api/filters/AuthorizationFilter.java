package me.moodcat.api.filters;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import me.moodcat.backend.UserBackend;
import me.moodcat.core.mappers.NotAuthorizedExceptionMapper;
import me.moodcat.database.entities.users.User;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * The {@code AuthorizationFilter} ensures that a current user can be injected
 * in the current request scope, given that the request provides a SoundCloud
 * OAuth token through its query parameters and the token can be verified against
 * the SoundCloud services.
 */
@Provider
@PreMatching
public class AuthorizationFilter implements ContainerRequestFilter {

    protected static final String TOKEN_PARAMETER = "token";

    private static final String CURRENT_USER_NAME = "current.user";

    private final UserBackend userBackend;

    private final NotAuthorizedExceptionMapper notAuthorizedExceptionMapper;

    @Inject
    public AuthorizationFilter(final UserBackend userBackend,
            final NotAuthorizedExceptionMapper notAuthorizedExceptionMapper) {
        this.userBackend = userBackend;
        this.notAuthorizedExceptionMapper = notAuthorizedExceptionMapper;
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final MultivaluedMap<String, String> parameters = containerRequestContext.getUriInfo()
                .getQueryParameters();
        final String token = parameters.getFirst(TOKEN_PARAMETER);

        if (!Strings.isNullOrEmpty(token)) {
            try {
                final User user = userBackend.loginUsingSoundCloud(token);
                containerRequestContext.setProperty(
                        Key.get(User.class, Names.named(CURRENT_USER_NAME))
                                .toString(), user);
            } catch (final NotAuthorizedException e) {
                final Response response = notAuthorizedExceptionMapper.toResponse(e);
                containerRequestContext.abortWith(response);
            }
        }

    }

}
