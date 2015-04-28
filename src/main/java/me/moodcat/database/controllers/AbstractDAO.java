package me.moodcat.database.controllers;

import javax.persistence.EntityManager;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * TODO: Add explanation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public abstract class AbstractDAO<T> {

    private EntityManager entityManager;

    protected AbstractDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public JPAQuery query() {
        return new JPAQuery(this.entityManager);
    }

    @Transactional
    public T persist(final T object) {
        this.entityManager.persist(object);
        log.debug("Persisted {}", object);
        return object;
    }

    @Transactional
    public T merge(final T object) {
        this.entityManager.merge(object);
        return object;
    }

    @Transactional
    public T remove(final T object) {
        this.entityManager.remove(object);
        return object;
    }

}
