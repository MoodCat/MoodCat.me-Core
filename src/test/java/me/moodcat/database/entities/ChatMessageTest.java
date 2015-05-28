package me.moodcat.database.entities;

import junitx.extensions.EqualsHashCodeTestCase;

/**
 * @author Jaap Heijligers
 */
public class ChatMessageTest extends EqualsHashCodeTestCase {

    public ChatMessageTest(final String name) {
        super(name);
    }

    @Override
    protected Object createInstance() throws Exception {
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(3);
        chatMessage.setAuthor("Henk");
        chatMessage.setMessage("Hello");
        final Room room = new Room();
        room.setName("Stub ROom");
        chatMessage.setRoom(room);
        chatMessage.setTimestamp(343234L);

        return chatMessage;
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(4);
        chatMessage.setAuthor("Henk");
        chatMessage.setMessage("Hello");
        final Room room = new Room();
        room.setName("Stub Room 2");
        chatMessage.setRoom(room);
        chatMessage.setTimestamp(343234L);

        return chatMessage;
    }

}
