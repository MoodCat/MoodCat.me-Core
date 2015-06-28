package me.moodcat.api.filters;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

/**
 * A limit rule describes how often a resource may be visited by a user
 * in a specific time unit.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitRule {

    /**
     * Number of visits.
     * 
     * @return the number of visits.
     */
    int amount() default 1;

    /**
     * Amount of time.
     * 
     * @return the amount of time.
     */
    int time() default 1;

    /**
     * The time unit.
     * 
     * @return the time unit.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

}
