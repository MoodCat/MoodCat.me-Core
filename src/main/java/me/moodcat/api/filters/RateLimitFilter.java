package me.moodcat.api.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.api.models.ExceptionResponse;
import me.moodcat.database.entities.User;

import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * The {@code RateLimitFilter} rejects filters based on the {@link RateLimit} annotation
 * on resource methods. It is used to filter out abusive messages.
 */
@Slf4j
@RateLimit
@Singleton
@javax.ws.rs.ext.Provider
public class RateLimitFilter implements ContainerRequestFilter {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * A map that temporary stores requests for a given method.
     */
    private final Multimap<Method, Entry> entries;

    /**
     * Provider to find the current user. Classifications and votes are bound to
     * a user, whilst session identifiers or IP addresses may vary.
     */
    private final Provider<User> currentUserProvider;

    @Inject
    public RateLimitFilter(@Named("current.user") final Provider<User> currentUserProvider) {
        this.entries = ArrayListMultimap.create();
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        final PostMatchContainerRequestContext ctx = (PostMatchContainerRequestContext) context;
        final Method method = ctx.getResourceMethod().getMethod();
        final RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        this.cleanUp();

        try {
            for (final LimitRule limitRule : rateLimit.rules()) {
                check(method, limitRule);
            }
        } catch (final RateLimitException e) {
            log.info("Rate limiter failed for {} ({})", e.getMessage(), e.getUuid());
            context.abortWith(buildResponse(e));
            return;
        }

        storeInvocation(method);
    }

    protected void storeInvocation(final Method method) {
        synchronized (this.entries) {
            this.entries.put(method, new Entry(System.currentTimeMillis(), currentUserProvider
                    .get().getId()));
        }
    }

    protected Response buildResponse(final RateLimitException e) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setUuid(e.getUuid().toString());
        exceptionResponse.setMessage(e.getMessage());

        return Response.status(HTTP_TOO_MANY_REQUESTS)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exceptionResponse)
                .build();
    }

    /**
     * Check a {@link LimitRule} for a {@code Method}.
     * 
     * @param method
     *            Method to check.
     * @param limitRule
     *            Limit rule to check.
     */
    protected void check(final Method method, final LimitRule limitRule) {
        final Integer currentUserId = currentUserProvider.get().getId();
        final long now = System.currentTimeMillis();
        final long ttl = limitRule.unit().toMillis(limitRule.time());
        final long count;

        synchronized (this.entries) {
            count = this.entries.get(method).stream()
                    .filter(entry -> currentUserId.equals(entry.getUserId()))
                    .filter(entry -> entry.time + ttl >= now)
                    .count();
        }

        if (count > limitRule.amount()) {
            throw new RateLimitException(method, limitRule);
        }
    }

    /**
     * Compute the Time-to-Live value for entries of this rule.
     * 
     * @param rules
     *            Set of rules on this resource method.
     * @return the Time-to-Live value.
     */
    protected long getTTL(final List<LimitRule> rules) {
        return rules.stream()
                .mapToLong(rule -> rule.unit().toMillis(rule.amount()))
                .max().orElse(0);
    }

    /**
     * Get the rules for a set of {@link RateLimit} annotations.
     * 
     * @param rateLimit
     *            annotations.
     * @return a list of rules.
     */
    protected List<LimitRule> getRules(final RateLimit rateLimit) {
        return Lists.newArrayList(rateLimit.rules());
    }

    /**
     * Clean up entries for all resource methods.
     */
    public void cleanUp() {
        synchronized (entries) {
            entries.keySet().forEach(this::cleanUp);
        }
    }

    /**
     * Clean up entries for a specific method.
     */
    protected void cleanUp(final Method method) {
        final List<LimitRule> rules = getRules(method.getAnnotation(RateLimit.class));
        final long now = System.currentTimeMillis();
        final long ttl = getTTL(rules);

        // Synchronized because ArrayListMultimap is not thread safe
        synchronized (entries) {
            final Collection<Entry> requests = entries.get(method);
            // Changes to the returned collection will update the underlying Multimap,
            // and vice versa.
            requests.removeIf(entry -> entry.getTime() + ttl < now);
        }
    }

    /**
     * Container for a request.
     */
    @Data
    static class Entry {

        /**
         * Time the request was made.
         */
        private final long time;

        /**
         * User ID for the request.
         */
        private final int userId;

    }

}
