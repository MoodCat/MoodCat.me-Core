package me.moodcat.api;

import algorithms.KNearestNeighbours;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import datastructures.dataholders.Pair;
import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;
import me.moodcat.backend.RoomBackend;
import me.moodcat.backend.RoomBackend.RoomInstance;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Room.RoomDistanceMetric;
import me.moodcat.mood.Mood;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The API for the room.
 */
@Path("/api/rooms/")
@Produces(MediaType.APPLICATION_JSON)
public class RoomAPI {

    /**
     * The number of miliseconds in a second, duh.
     */
    private static final int SECOND_OF_MILISECONDS = 1000;

    /**
     * The backend of the room.
     */
    private final RoomBackend backend;

    /**
     * The database access object for the rooms.
     */
    private final RoomDAO roomDAO;

    @Inject
    @VisibleForTesting
    public RoomAPI(final RoomBackend backend, final RoomDAO roomDAO) {
        this.backend = backend;
        this.roomDAO = roomDAO;
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
    public List<RoomModel> getRooms(@QueryParam("mood") final List<String> moods,
            @QueryParam("limit") @DefaultValue("25") final int limit) {
        final VAVector targetVector = Mood.createTargetVector(moods);

        final Room idealroom = new Room();
        idealroom.setVaVector(targetVector);

        final List<Room> allRooms = roomDAO.listRooms();

        final KNearestNeighbours<Room> knearest = new KNearestNeighbours<Room>(allRooms,
                new RoomDistanceMetric());
        final Collection<Pair<Double, Room>> knearestResult = knearest.getNearestNeighbours(limit,
                idealroom);

        return knearestResult.stream()
                .map(Pair::getRight)
                .map(this::resolveRoomInstance)
                .map(RoomAPI::transform)
                .collect(Collectors.toList());
    }

    private RoomBackend.RoomInstance resolveRoomInstance(Room room) {
        return backend.getRoomInstance(room.getId());
    }

    /**
     * Transform a {@link RoomInstance} into a
     * @param roomInstance
     * @return
     */
    public static RoomModel transform(RoomBackend.RoomInstance roomInstance) {
        final RoomModel roomModel = new RoomModel();
	    roomModel.setId(roomInstance.getRoom().getId());
        roomModel.setName(roomInstance.getName());
        roomModel.setSong(SongModel.transform(roomInstance.getCurrentSong()));
        roomModel.setTime(roomInstance.getCurrentTime());
        return roomModel;
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
    public RoomModel getRoom(@PathParam("id") final int roomId) {
        return transform(backend.getRoomInstance(roomId));
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
        return backend.getRoomInstance(roomId).getMessages();
    }

    /**
     * Post a message to a room.
     *
     * @param msg
     *            The message to post.
     * @param roomId
     *            The id of the room.
     * @return The chatmessage if storage was succesful.
     */
    @POST
    @Path("{id}/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ChatMessage postChatMessage(final ChatMessage msg, @PathParam("id") final int roomId) {
        final RoomBackend.RoomInstance roomInstance = backend.getRoomInstance(roomId);
        msg.setRoom(roomInstance.getRoom());
        msg.setTimestamp(System.currentTimeMillis() / SECOND_OF_MILISECONDS);

        roomInstance.sendMessage(msg);
        return msg;
    }

    @GET
    @Path("{id}/time")
    @Transactional
    public int getCurrentTime(@PathParam("id") final int roomId) {
        return backend.getRoomInstance(roomId).getCurrentTime();
    }

}
