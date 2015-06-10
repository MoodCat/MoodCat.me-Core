package me.moodcat.backend.rooms;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

/**
 * The ChatMessageIdGenerator generates ids for chat messages in a room.
 */
public class ChatMessageIdGenerator {

    public static final int START_CHAT_MESSAGE_INDEX = 0;

    private final AtomicInteger messageIndex;

    /**
     * Create a new {@code ChatMessageIdGenerator}.
     *
     * @param room
     *            Room to generate.
     */
    public ChatMessageIdGenerator(final Room room) {
        this.messageIndex = getAtomicInteger(room.getChatMessages());
    }

    private static AtomicInteger getAtomicInteger(final Collection<ChatMessage> messages) {
        assert messages != null : "Messages should not be null";
        return new AtomicInteger(messages.stream()
                .mapToInt(ChatMessage::getId)
                .max().orElse(START_CHAT_MESSAGE_INDEX));
    }

    /**
     * Generate a new id.
     *
     * @return the newly generated id.
     */
    public int generateId() {
        return this.messageIndex.incrementAndGet();
    }

}
