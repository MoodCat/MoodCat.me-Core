package me.moodcat.api.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.User;

import org.jboss.resteasy.core.interception.ContainerResponseContextImpl;
import org.jboss.resteasy.specimpl.BuiltResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * This Filter adds points for some requests.
 */
@javax.ws.rs.ext.Provider
@AwardPoints
public class AwardPointsFilter implements ContainerResponseFilter {

    private final Provider<User> currentUserProvider;

    private final Provider<UserDAO> userDAOProvider;

    @Inject
    public AwardPointsFilter(@Named("current.user") final Provider<User> currentUserProvider,
            final Provider<UserDAO> userDAOProvider) {
        this.currentUserProvider = currentUserProvider;
        this.userDAOProvider = userDAOProvider;
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext,
            final ContainerResponseContext containerResponseContext)
            throws IOException {
        final ContainerResponseContextImpl impl = (ContainerResponseContextImpl) containerResponseContext;
        final BuiltResponse buildResponse = impl.getJaxrsResponse();
        final Annotation[] annotations = buildResponse.getAnnotations();

        final UserDAO userDAO = userDAOProvider.get();
        final AwardPoints awardPoints = getFromArray(annotations, AwardPoints.class);
        
        if (awardPoints != null) {
            userDAO.incrementPoints(currentUserProvider.get(), awardPoints.value());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends V, V> T getFromArray(final V[] contents, final Class<T> type) {
        if (contents != null) {
            for (V value : contents) {
                if (type.isInstance(value)) {
                    return (T) value;
                }
            }
        }
        return null;
    }

}
