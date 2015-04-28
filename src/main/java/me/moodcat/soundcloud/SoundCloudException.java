package me.moodcat.soundcloud;

public class SoundCloudException extends Exception {

    /**
     * Exception thrown by SoundCloud extraction.
     *
     * @param msg Description of the error
     */
    SoundCloudException(String msg) {
        super(msg);
    }
}
