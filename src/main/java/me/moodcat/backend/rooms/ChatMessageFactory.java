package me.moodcat.backend.rooms;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.users.User;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

/**
 * ChatMessageFactory.
 */
public class ChatMessageFactory {

    private final Provider<UserDAO> userDAOProvider;

    @Inject
    public ChatMessageFactory(final Provider<UserDAO> userDAOProvider) {
        this.userDAOProvider = userDAOProvider;
    }

    /**
     * Create a ChatMessage entity according to the ChatMessage instance
     * provided by the backend.
     * 
     * @param room
     *            The room the chat message was posted in.
     * @param chatMessageInstance
     *            The ChatMessage.
     * @return The ChatMessage entity to store in the database.
     */
    @Transactional
    public ChatMessage create(final Room room, final ChatMessageInstance chatMessageInstance) {
        final ChatMessage chatMessage = new ChatMessage();
        final User user = userDAOProvider.get().findById(
                chatMessageInstance.getUserId());
        chatMessage.setUser(user);
        chatMessage.setMessage(chatMessageInstance.getMessage());
        chatMessage.setTimestamp(chatMessageInstance.getTimestamp());
        chatMessage.setId(chatMessageInstance.getId());
        chatMessage.setRoom(room);
        return chatMessage;
    }

}
