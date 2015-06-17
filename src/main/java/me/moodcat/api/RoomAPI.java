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

import me.moodcat.api.filters.AwardPoints;
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
import me.moodcat.database.entities.User;
import me.moodcat.backend.Vote;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

/**
 * The API for the room.
 */
@Path("/api/rooms/")
@Produces(MediaType.APPLICATION_JSON)
public class RoomAPI {

    private static final int VOTES_POINTS_AWARD = 2;

    /**
     * The backend of the room.
     */
    private final RoomBackend backend;

    /**
     * The database access object for the rooms.
     */
    private final RoomDAO roomDAO;

    /**
     * Current User provider.
     */
    private final Provider<User> currentUserProvider;

    @Inject
    @VisibleForTesting
    public RoomAPI(final RoomBackend backend, final RoomDAO roomDAO,
            @Named("current.user") final Provider<User> currentUserProvider) {
        this.backend = backend;
        this.roomDAO = roomDAO;
        this.currentUserProvider = currentUserProvider;
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
     * Get all the message of the room that happened later than where posted after the chatmessage
     * with the corresponding messageId.
     * 
     * @param roomId
     *            The room the messages were placed in.
     * @param chatMessageId
     *            The message id we want to obtain later messages of.
     * @return A list of messages that happened later than the provided chatMessageId. Can be empty.
     */
    @GET
    @Path("{id}/messages/{chatMessageId}")
    public List<ChatMessageModel> getMessages(@PathParam("id") final int roomId,
            @PathParam("chatMessageId") final int chatMessageId) {
        return backend.getRoomInstance(roomId).getMessages().stream()
                .filter((message) -> message.getId() > chatMessageId)
                .collect(Collectors.toList());
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
        return backend.getRoomInstance(roomId).sendMessage(msg, currentUserProvider.get());
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

    /**
     * Process a vote to a song. A vote is either "like" or "dislike".
     *
     * @param vote
     *            The vote.
     * @return The song object, if the process was succesful.
     */
    @POST
    @Path("{id}/vote/{vote}")
    @Transactional
    @AwardPoints(VOTES_POINTS_AWARD)
    public RoomModel voteSong(@PathParam("id") final int roomId,
            @PathParam("vote") final String vote) {
        final RoomInstance roomInstance = this.backend.getRoomInstance(roomId);

        Vote voteValue = Vote.valueOf(vote.toUpperCase());

        roomInstance.addVote(currentUserProvider.get(), voteValue);

        return transform(roomInstance);
    }
}
