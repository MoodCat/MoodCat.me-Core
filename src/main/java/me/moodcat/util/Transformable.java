package me.moodcat.util;

/**
 * A transformable is an object that can be transformed into another object
 */
public interface Transformable<T> {

    /**
     * Transform this object.
     *
     * @return Transformed object
     */
    T transform();

}
