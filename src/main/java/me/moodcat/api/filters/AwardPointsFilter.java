package me.moodcat.api.filters;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * This Filter adds points for some requests.
 *
 */
@javax.ws.rs.ext.Provider
@AwardPoints
public class AwardPointsFilter implements ContainerRequestFilter {

    private final Provider<User> currentUserProvider;
    private final Provider<UserDAO> userDAOProvider;

    @Inject
    public AwardPointsFilter(@Named("current.user") final Provider<User> currentUserProvider,
                             final Provider<UserDAO> userDAOProvider) {
        this.currentUserProvider = currentUserProvider;
        this.userDAOProvider = userDAOProvider;
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final PostMatchContainerRequestContext context = (PostMatchContainerRequestContext) containerRequestContext;
        final UserDAO userDAO = userDAOProvider.get();
        final AwardPoints awardPoints = context.getResourceMethod().getMethod().getAnnotation(AwardPoints.class);
        userDAO.incrementPoints(currentUserProvider.get(), awardPoints.value());
    }
}
