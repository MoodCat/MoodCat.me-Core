package me.moodcat.api;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import me.moodcat.database.controllers.ChatDAO;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Room.RoomDistanceMetric;
import me.moodcat.mood.Mood;
import algorithms.KNearestNeighbours;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import datastructures.dataholders.Pair;

/**
 * The API for the room.
 *
 * @author Jaap Heijligers
 */
@Path("/api/rooms/")
@Produces(MediaType.APPLICATION_JSON)
public class RoomAPI {

    /**
     * The number of miliseconds in a second, duh.
     */
    private static final int SECOND_OF_MILISECONDS = 1000;

    /**
     * The DAO of the room.
     */
    private final RoomDAO roomDAO;

    /**
     * Access to the chat DAO.
     */
    private final ChatDAO chatDAO;

    @Inject
    @VisibleForTesting
    public RoomAPI(final RoomDAO roomDAO, final ChatDAO chatDAO) {
        this.roomDAO = roomDAO;
        this.chatDAO = chatDAO;
    }

    /**
     * Get all the rooms that are sorted on how close they are to the provided moods.
     *
     * @param moods
     *            The moods we want to have rooms for.
     * @param limit
     *            The number of rooms to retrieve.
     * @return The list of rooms that are close to the provided moods.
     */
    @GET
    @Transactional
    public List<Room> getRooms(@QueryParam("mood") final List<String> moods,
            @QueryParam("limit") @DefaultValue("25") final int limit) {
        final VAVector targetVector = Mood.createTargetVector(moods);

        final Room idealroom = new Room();
        idealroom.setArousal(targetVector.getArousal());
        idealroom.setValence(targetVector.getValence());

        final List<Room> allRooms = roomDAO.listRooms();

        final KNearestNeighbours<Room> knearest = new KNearestNeighbours<Room>(allRooms,
                new RoomDistanceMetric());
        final Collection<Pair<Double, Room>> knearestResult = knearest.getNearestNeighbours(limit,
                idealroom);

        return knearestResult.stream()
                .map(neighbour -> neighbour.getRight())
                .collect(Collectors.toList());
    }

    /**
     * Get the room according to the provided id.
     *
     * @param roomId
     *            The id of the room to find.
     * @return The room according to the id.
     * @throws IllegalArgumentException
     *             If the roomId is null.
     */
    @GET
    @Path("{id}")
    @Transactional
    public Room getRoom(@PathParam("id") final Integer roomId) throws IllegalArgumentException {
        if (roomId == null) {
            throw new IllegalArgumentException();
        }

        return roomDAO.findById(roomId.intValue());
    }

    /**
     * Get all the messages of the room.
     *
     * @param roomId
     *            The id of the room to retrieve messages from.
     * @return The list of messages of the room.
     */
    @GET
    @Path("{id}/messages")
    @Transactional
    public List<ChatMessage> getMessages(@PathParam("id") final int roomId) {
        return roomDAO.listMessages(roomId);
    }

    /**
     * Post a message to a room.
     *
     * @param msg
     *            The message to post.
     * @param id
     *            The id of the room.
     * @return The chatmessage if storage was succesful.
     */
    @POST
    @Path("{id}/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ChatMessage postChatMessage(final ChatMessage msg, @PathParam("id") final int id) {
        msg.setRoom(roomDAO.findById(id));
        msg.setTimestamp(System.currentTimeMillis() / SECOND_OF_MILISECONDS);

        chatDAO.persist(msg);
        return msg;
    }

}
