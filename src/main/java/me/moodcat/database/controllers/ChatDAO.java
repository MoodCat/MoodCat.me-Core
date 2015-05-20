package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QChatMessage.chatMessage;

import java.util.List;

import javax.persistence.EntityManager;

import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Manager that can fetch messages from the database.
 *
 * @author JeremybellEU
 */
public class ChatDAO extends AbstractDAO<ChatMessage> {

    /**
     * The number of chat messages we want to return for each request.
     */
    private static final long NUMBER_OF_CHAT_MESSAGE = 10;

    @Inject
    public ChatDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Get the last {@link #NUMBER_OF_CHAT_MESSAGE} of the specified room.
     *
     * @param room
     *            The room to fetch the messages for
     * @return A list of messages from this room
     */
    @Transactional
    public List<ChatMessage> listByRoom(final Room room) {
        return this.query().from(chatMessage)
                .where(chatMessage.room.eq(room))
                .limit(NUMBER_OF_CHAT_MESSAGE)
                .list(chatMessage);
    }

    /**
     * Get the last {@link #NUMBER_OF_CHAT_MESSAGE} of the room with the specified roomId.
     *
     * @param roomId
     *            The id of the room to fetch the messages for
     * @return A list of messages from this room
     */
    @Transactional
    public List<ChatMessage> listByRoomId(final int roomId) {
        return this.query().from(chatMessage)
                .where(chatMessage.room.id.eq(roomId))
                .limit(NUMBER_OF_CHAT_MESSAGE)
                .list(chatMessage);
    }

    /**
     * Persists a message object in the database.
     * 
     * @param msg
     *            The ChatMessage object to persist.
     */
    @Transactional
    public void addMessage(ChatMessage msg) {
        this.persist(msg);
    }
}
