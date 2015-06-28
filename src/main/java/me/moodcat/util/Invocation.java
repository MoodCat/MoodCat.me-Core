package me.moodcat.util;

import javax.ws.rs.client.WebTarget;

/**
 * An invocation to a HTTPClient
 *
 * @param <T>
 *            return type
 */
@FunctionalInterface
public interface Invocation<T> {

    /**
     * Perform the call to the HttpClient.
     *
     * @param webTarget
     *            target to interact with
     * @return response
     * @throws Exception
     *             any exception that occurs
     */
    T perform(WebTarget webTarget) throws Exception;

}
