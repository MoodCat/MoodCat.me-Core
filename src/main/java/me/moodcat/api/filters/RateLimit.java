package me.moodcat.api.filters;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * Limit access on this resource.
 */
@Documented
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rule set for this resource.
     * 
     * @return the {@link LimitRule LimitRules} for this resource.
     */
    LimitRule[] rules() default {};

}
