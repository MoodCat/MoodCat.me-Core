package me.moodcat.soundcloud;

public class SoundCloudException extends Exception {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = -879808709985997690L;

    /**
     * Exception thrown by SoundCloud extraction.
     *
     * @param msg
     *            Description of the error
     */
    SoundCloudException(final String msg) {
        super(msg);
    }
}
