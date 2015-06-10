package me.moodcat.api;

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

import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.api.models.NowPlaying;
import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;
import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.backend.rooms.RoomInstance;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.mood.Mood;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * The API for the room.
 */
@Path("/api/rooms/")
@Produces(MediaType.APPLICATION_JSON)
public class RoomAPI {

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

        return roomDAO.queryRooms(targetVector, limit)
                .stream()
                .map(this::resolveRoomInstance)
                .map(RoomAPI::transform)
                .collect(Collectors.toList());
    }

    private RoomInstance resolveRoomInstance(final Room room) {
        return backend.getRoomInstance(room.getId());
    }

    /**
     * Transform a {@link RoomInstance} into a roommodel.
     *
     * @param roomInstance
     *            The instance to create a roommodel from.
     * @return The roommodel that represents the roominstance.
     */
    @Transactional
    public static RoomModel transform(final RoomInstance roomInstance) {
        final RoomModel roomModel = new RoomModel();
        final SongModel songModel = SongModel.transform(roomInstance.getCurrentSong());
        final NowPlaying nowPlaying = new NowPlaying(roomInstance.getCurrentTime(), songModel);

        roomModel.setId(roomInstance.getId());
        roomModel.setName(roomInstance.getName());
        roomModel.setNowPlaying(nowPlaying);
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
    public List<ChatMessageModel> getMessages(@PathParam("id") final int roomId) {
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
    public ChatMessageModel postChatMessage(final ChatMessageModel msg,
            @PathParam("id") final int roomId) {
        return backend.getRoomInstance(roomId).sendMessage(msg);
    }

    /**
     * Retrieve whats playing now.
     *
     * @param roomId
     *            The id of the room.
     * @return
     *         Whats currently playing in the room
     */
    @GET
    @Path("{id}/now-playing")
    @Transactional
    public NowPlaying getCurrentTime(@PathParam("id") final int roomId) {
        final RoomInstance roomInstance = backend.getRoomInstance(roomId);
        final Song song = roomInstance.getCurrentSong();

        final NowPlaying nowPlaying = new NowPlaying();
        nowPlaying.setSong(SongModel.transform(song));
        nowPlaying.setTime(roomInstance.getCurrentTime());
        return nowPlaying;
    }

}
