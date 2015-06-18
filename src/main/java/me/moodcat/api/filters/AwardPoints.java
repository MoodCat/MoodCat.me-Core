package me.moodcat.api.filters;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotates resource methods that award points to the user.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface AwardPoints {

    /**
     * Amount of points to award.
     */
    int value() default 0;

}
