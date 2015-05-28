package me.moodcat.database.bootstrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks that a test requires a bootstrap
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface TestBootstrap {

    /**
     * The configuration files to load
     * @return the configuration files to load
     */
    String[] value() default {};

}
