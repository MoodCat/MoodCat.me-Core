package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QChatMessage.chatMessage;
import static me.moodcat.database.entities.QRoom.room;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;

import com.google.inject.Inject;

/**
 * The DAO for rooms.
 */
public class RoomDAO extends AbstractDAO<Room> {

    /**
     * The number of chat messages we want to return for each request.
     */
    private static final long NUMBER_OF_CHAT_MESSAGE = 10;

    @Inject
    public RoomDAO(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Find a room by id.
     *
     * @param id
     *            the id of the room
     * @return the {@link Room}
     */
    @Transactional
    public Room findById(final int id) {
        return ensureExists(this.query().from(room)
                .where(room.id.eq(id))
                .singleResult(room));
    }

    /**
     * Obtain all the rooms from the database.
     *
     * @return The list of all rooms.
     */
    @Transactional
    public List<Room> listRooms() {
        return this.query().from(room).list(room);
    }

    /**
     * Obtain all the rooms from the database limited to lim.
     *
     * @param lim
     *            The number of rooms to return.
     * @return The list of lim rooms.
     */
    @Transactional
    public List<Room> listRooms(final int lim) {
        return this.query().from(room).limit(lim).list(room);
    }

    /**
     * Get the last {@link #NUMBER_OF_CHAT_MESSAGE} of the room with the specified roomId.
     *
     * @param id
     *            The id of the room to fetch the messages for
     * @return A list of messages from this room
     */
    @Transactional
    public List<ChatMessage> listMessages(final int id) {
        return this.query().from(chatMessage)
                .where(chatMessage.room.id.eq(id))
                .limit(NUMBER_OF_CHAT_MESSAGE)
                .list(chatMessage);
    }

    /**
     * Query rooms in space.
     * 
     * @param vector
     *            Vector to compare with
     * @param limit
     *            Limit results
     * @return a list of rooms
     */
    @Transactional
    public List<Room> queryRooms(final VAVector vector, final int limit) {
        return query().from(room)
                .orderBy(room.vaVector.location.distance(vector.getLocation()).asc())
                .limit(limit)
                .list(room);
    }

}
