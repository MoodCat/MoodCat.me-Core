package me.moodcat.api.models;

import lombok.Getter;
import lombok.Setter;

/**
 * The response to the frontend.
 */
public class ExceptionResponse {

    /**
     * The unique id for the exception.
     *
     * @param uuid
     *            The new unique id of this response.
     * @return The unique id of this response.
     */
    @Getter
    @Setter
    private String uuid;

    /**
     * The exception message.
     *
     * @param message
     *            The message to inform the user.
     * @return The message of this response.
     */
    @Getter
    @Setter
    private String message;

}