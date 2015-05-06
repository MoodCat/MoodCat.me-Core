package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QChatMessage.chatMessage;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

import com.google.inject.Inject;

public class ChatDAO extends AbstractDAO<ChatMessage> {

    private static final long NUMBER_OF_CHAT_MESSAGE = 10;

    @Inject
    public ChatDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<ChatMessage> listByRoomId(final Room roomId) {
        return this.query().from(chatMessage)
                .where(chatMessage.roomId.eq(roomId))
                .limit(NUMBER_OF_CHAT_MESSAGE)
                .list(chatMessage);
    }
}
