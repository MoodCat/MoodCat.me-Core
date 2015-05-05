package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QChatMessage.chatMessage;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

public class ChatDAO extends AbstractDAO<ChatMessage> {

    private static final long NUMBER_OF_CHAT_MESSAGE = 10;

    protected ChatDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<ChatMessage> listByRoomId(final Room roomId) {
        return this.query().from(chatMessage)
                .where(chatMessage.roomId.eq(roomId))
                .limit(NUMBER_OF_CHAT_MESSAGE)
                .list(chatMessage);
    }
}
