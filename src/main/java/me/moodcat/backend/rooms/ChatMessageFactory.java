package me.moodcat.backend.rooms;

import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.User;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

/**
 * ChatMessageFactory.
 */
public class ChatMessageFactory {

    private final Provider<UserDAO> userDAOProvider;

    @Inject
    public ChatMessageFactory(Provider<UserDAO> userDAOProvider) {
        this.userDAOProvider = userDAOProvider;
    }

    @Transactional
    public ChatMessage create(Room room, ChatMessageInstance chatMessageInstance) {
        final ChatMessage chatMessage = new ChatMessage();
        final User user = userDAOProvider.get()
                .findById(chatMessageInstance.getUserId());
        chatMessage.setUser(user);
        chatMessage.setMessage(chatMessageInstance.getMessage());
        chatMessage.setTimestamp(chatMessageInstance.getTimestamp());
        chatMessage.setId(chatMessageInstance.getId());
        chatMessage.setRoom(room);
        return chatMessage;
    }

}
