package me.moodcat.soundcloud;

import javax.ws.rs.client.WebTarget;

/**
 * An invocation to the Soundcloud API.
 *
 * @param <T>
 *            return type
 */
@FunctionalInterface
interface Invocation<T> {

    /**
     * Perform the call to the SoundCloud API.
     *
     * @param webTarget
     *            target to interact with
     * @return response
     * @throws Exception
     *             any exception that occurs
     */
    T perform(WebTarget webTarget) throws Exception;
}
