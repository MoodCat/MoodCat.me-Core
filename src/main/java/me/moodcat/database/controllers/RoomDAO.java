package me.moodcat.database.controllers;

import static me.moodcat.database.entities.QChatMessage.chatMessage;
import static me.moodcat.database.entities.QRoom.room;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Room.RoomDistanceMetric;
import algorithms.KNearestNeighbours;

import com.google.inject.Inject;

import datastructures.dataholders.Pair;

/**
 * The DAO for rooms.
 *
 * @author Jaap Heijligers
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

    @Transactional
    public List<Room> listRooms(final int lim) {
        return this.query().from(room).limit(lim).list(room);
    }

    @Transactional
    public List<Room> listRooms(VAVector targetVector, final int lim) {
        Room idealroom = new Room();
        idealroom.setArousal(targetVector.getArousal());
        idealroom.setValence(targetVector.getValence());

        List<Room> allrooms = this.query().from(room).list(room);
        KNearestNeighbours<Room> knearest = new KNearestNeighbours<Room>(allrooms,
                new RoomDistanceMetric());
        Collection<Pair<Double, Room>> knearestResult = knearest.getNearestNeighbours(lim,
                idealroom);

        return knearestResult.stream()
                .map(neighbour -> neighbour.getRight())
                .collect(Collectors.toList());
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

}
