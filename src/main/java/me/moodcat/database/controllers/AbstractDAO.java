package me.moodcat.database.controllers;

import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public abstract class AbstractDAO<T> {

    private EntityManager entityManager;

    protected AbstractDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public JPAQuery query() {
        return new JPAQuery(entityManager);
    }

    @Transactional
    public T persist(T object) {
        entityManager.persist(object);
        log.debug("Persisted {}", object);
        return object;
    }

    @Transactional
    public T merge(T object) {
        entityManager.merge(object);
        return object;
    }

    @Transactional
    public T remove(T object) {
        entityManager.remove(object);
        return object;
    }

}
