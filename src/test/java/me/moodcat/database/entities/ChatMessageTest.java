package me.moodcat.database.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Jaap Heijligers
 */
public class ChatMessageTest {

    @Test
    public void chatMessageTest() {
        ChatMessage chatMessage1 = createDefaultChatMessage();
        ChatMessage chatMessage2 = createDefaultChatMessage();
        assertEquals(chatMessage1, chatMessage2);
    }

    private ChatMessage createDefaultChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(3);
        chatMessage.setAuthor("Henk");
        chatMessage.setMessage("Hello");
        Room room = new Room();
        room.setName("Stub ROom");
        chatMessage.setRoom(room);
        chatMessage.setTimestamp(343234L);

        return chatMessage;
    }

}
