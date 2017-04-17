package me.moodcat.api.filters;

import java.lang.reflect.Method;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Exception thrown by the rate limiter.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = true)
public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = -2602661296357147972L;

    private static final String RATE_LIMIT_EXCEEDED_STRING = "You exceeded the rate limit. Try again later.";

    /**
     * @param uuid
     *            the new {@code UUID} for this exception.
     * @return the {@code UUID} for this exception.
     */
    private final UUID uuid;

    /**
     * @param method
     *            the new {@code Method} for this exception.
     * @return the {@code Method} for this exception.
     */
    private final Method method;

    /**
     * @param rateLimitRule
     *            the new {@link LimitRule} for this exception.
     * @return the {@code LimitRule} for this exception.
     */
    private final LimitRule rateLimitRule;

    /**
     * Create a new {@code RateLimitException}.
     * 
     * @param method
     *            Method that failed.
     * @param rateLimitRule
     *            Limit rule that failed.
     */
    public RateLimitException(final Method method, final LimitRule rateLimitRule) {
        super(RATE_LIMIT_EXCEEDED_STRING);
        this.uuid = UUID.randomUUID();
        this.method = method;
        this.rateLimitRule = rateLimitRule;
    }

}
