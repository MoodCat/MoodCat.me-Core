package me.moodcat.database.controllers;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.ChatMessage;
import com.google.inject.Inject;

/**
 * Manager that can fetch messages from the database.
 */
public class ChatDAO extends AbstractDAO<ChatMessage> {

    @Inject
    public ChatDAO(final EntityManager entityManager) {
        super(entityManager);
    }
}
