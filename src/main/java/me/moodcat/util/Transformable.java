package me.moodcat.util;

/**
 * A transformable is an object that can be transformed into another object.
 *
 * @param <T>
 *            The class it can transform into.
 */
public interface Transformable<T> {

    /**
     * Transform this object.
     *
     * @return Transformed object
     */
    T transform();

}
