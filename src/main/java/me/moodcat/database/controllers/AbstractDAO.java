package me.moodcat.database.controllers;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * Manages data to be retrieved or inserted into the database.
 * 
 * @param <T>
 *            The type to be saved.
 */
@Slf4j
public abstract class AbstractDAO<T> {

    /**
     * Manager that can talk to the actual database.
     */
    private final EntityManager entityManager;

    protected AbstractDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getManager() {
        return entityManager;
    }

    /**
     * Query the database.
     *
     * @return a {@link JPAQuery} for the current {@link EntityManager}
     */
    protected JPAQuery query() {
        return new JPAQuery(this.entityManager);
    }

    /**
     * Persist an entity.
     *
     * @param object
     *            entity to persist
     * @return the persisted entity
     */
    @Transactional
    public T persist(final T object) {
        log.debug("Persisting {}", object);
        this.entityManager.persist(object);
        return object;
    }

    /**
     * Update an entity.
     *
     * @param object
     *            entity to update
     * @return the updated entity
     */
    @Transactional
    public T merge(final T object) {
        log.debug("Merging {}", object);
        return this.entityManager.merge(object);
    }

    /**
     * Remove an entity.
     *
     * @param object
     *            entity to remove
     * @return the removed entity
     */
    @Transactional
    public T remove(final T object) {
        this.entityManager.remove(object);
        log.debug("Removed {}", object);
        return object;
    }

    /**
     * Check that an entity is not null.
     *
     * @param entity
     *            entity that should not be null
     * @return the entity
     * @throws EntityNotFoundException
     *             if the entity could not be found
     */
    protected T ensureExists(final T entity) {
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

}
