package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QClassification.classification;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.Classification;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

;

/**
 * Data-access object for classification objects.
 */
public class ClassificationDAO extends AbstractDAO<Classification> {

    @Inject
    public ClassificationDAO(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Check if a {@link Classification} exists for a given {@link User} and {@link Song}.
     *
     * @param user
     *            User for the classification.
     * @param song
     *            Song for the classificatoin.
     * @return true if a classification exists.
     */
    @Transactional
    public boolean exists(final User user, final Song song) {
        return query().from(classification)
                .where(classification.user.eq(user)
                        .and(classification.song.eq(song)))
                .exists();
    }

}
