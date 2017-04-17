package me.moodcat.api.filters;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * Annotates resource methods that should only be executed when you are admin.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminFiltered {

}
