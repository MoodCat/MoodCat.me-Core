package me.moodcat.backend.rooms;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.Vote;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.ChatMessage;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.users.User;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * The instance object of the rooms.
 */
@Slf4j
public class RoomInstance {

    /**
     * Number of selected songs from the database.
     */
    public static final int NUMBER_OF_SELECTED_SONGS = 25;
    
    /**
     * Number of chat messages to cache for each room.
     */
    public static final int MAXIMAL_NUMBER_OF_CHAT_MESSAGES = 100;
    
    /**
     * How much the vector should approach the room vector.
     */
    public static final double CLASSIFY_GROW_FACTOR = 0.02;

    private static final int MESSAGE_FLOODING_TIMEOUT = 10;

    private static final int MESSAGE_FLOODING_MESSAGE_AMOUNT = 4;

    /**
     * {@link SongInstanceFactory} to create {@link SongInstance SongInstances} with.
     */
    private final SongInstanceFactory songInstanceFactory;

    /**
     * {@link me.moodcat.backend.UnitOfWorkSchedulingServiceImpl} to schedule tasks in a unit of work.
     */
    private final UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    /**
     * The profanity checker to filter out 'bad' chatmessages.
     */
    private final ProfanityChecker profanityChecker;

    /**
     * The room index.
     *
     * @return the room id
     */
    @Getter
    private final int id;

    /**
     * The room name.
     *
     * @return the room name
     */
    @Getter
    private final String name;

    /**
     * The {@link RoomInstanceInUnitOfWorkFactory}.
     */
    private final RoomInstanceInUnitOfWorkFactory roomInstanceInUnitOfWorkFactory;

    /**
     * Keep track of the message index.
     */
    private final ChatMessageIdGenerator chatMessageIdGenerator;
    
    private Provider<SongDAO> songDAOProvider;

    /**
     * The cached messages in order to speed up retrieval.
     */
    private final Deque<ChatMessageInstance> messages;

    /**
     * The current song.
     */
    private final AtomicReference<SongInstance> currentSong;

    /**
     * Has changed flag.
     */
    private final AtomicBoolean hasChanged;

    /**
     * The votes of the users for the current song.
     */
    private final Map<User, Vote> votes;

    @AssistedInject
    public RoomInstance(final SongInstanceFactory songInstanceFactory,
            final RoomInstanceInUnitOfWorkFactory roomInstanceInUnitOfWorkFactory,
            final UnitOfWorkSchedulingService unitOfWorkSchedulingService,
            final ProfanityChecker profanityChecker,
            final Provider<SongDAO> songDAOProvider,
            @Assisted final Room room) {
        Preconditions.checkNotNull(room);
        this.profanityChecker = profanityChecker;
        this.songInstanceFactory = songInstanceFactory;
        this.roomInstanceInUnitOfWorkFactory = roomInstanceInUnitOfWorkFactory;
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;
        this.songDAOProvider = songDAOProvider;
        this.votes = Maps.newConcurrentMap();

        this.id = room.getId();
        this.name = room.getName();
        this.chatMessageIdGenerator = new ChatMessageIdGenerator(room);
        this.messages = getChatMessageModels(room.getChatMessages());
        this.currentSong = new AtomicReference<SongInstance>();
        this.hasChanged = new AtomicBoolean(false);

        this.scheduleSyncTimer();
        this.startPlaying(room.getCurrentSong());
        log.info("Initialized room instance {}", this);
    }

    private static LinkedList<ChatMessageInstance> getChatMessageModels(
            final Collection<ChatMessage> messages) {
        return Lists.newLinkedList(messages.stream()
                .map(ChatMessageInstance::create).collect(Collectors.toList()));
    }

    protected Future<?> interactWithRoom(final RoomInstanceInUnitOfWorkHandler handler) {
        return unitOfWorkSchedulingService.performInUnitOfWork(() -> {
            RoomInstanceInUnitOfWork instance = roomInstanceInUnitOfWorkFactory.create(id);
            handler.handle(instance);
        });
    }

    /**
     * Play a next song. Will fetch from the history if no songs can be found.
     */
    public Future<?> playNext() {
        return interactWithRoom(instance -> {
            processVotes(instance);
            Song song = instance.nextSong();
            startPlaying(song);
            instance.merge();
        });
    }

    /**
     * Start playing a song. Should only be called in a {@code UnitOfWork}.
     * Used in the constructor to start playing the initial song.
     * Then used in the {@link RoomInstance#playNext()} to start playing
     * new songs.
     *
     * @param song
     *            Song to be played.
     */
    @RunInUnitOfWork
    protected void startPlaying(final Song song) {
        final SongInstance songInstance = songInstanceFactory.create(song);
        this.currentSong.set(songInstance);
        log.info("Room {} now playing {}", this.id, song);

        // Observer: Play the next song when the song is finished
        songInstance.addObserver(this::playNext);
    }

    @RunInUnitOfWork
    private void processVotes(final RoomInstanceInUnitOfWork instance) {
        int nettoVotes = this.votes.values().stream()
                .mapToInt(Vote::getValue)
                .sum();

        if (nettoVotes < 0) {
            instance.excludeRoomFromSong();
        } else if (nettoVotes > 0) {
            Song previousSong = instance.getCurrentSong();
            
            final VAVector adjusted = adjustSongVectorToRoomVector(instance.getVector(),
                previousSong.getValenceArousal());
            
            previousSong.setValenceArousal(adjusted);
            songDAOProvider.get().merge(previousSong);
        }

        this.votes.clear();
    }

    private VAVector adjustSongVectorToRoomVector(final VAVector roomVector, final VAVector songVector) {
        final VAVector adjustment = roomVector.subtract(songVector);
        
        return new VAVector(
            Math.min(1, Math.max(-1, songVector.getValence() + adjustment.getValence() * CLASSIFY_GROW_FACTOR)),
            Math.min(1, Math.max(-1, songVector.getArousal() + adjustment.getArousal() * CLASSIFY_GROW_FACTOR))
        );
    }

    /**
     * Sync room messages to the database.
     */
    private void scheduleSyncTimer() {
        this.unitOfWorkSchedulingService.scheduleAtFixedRate(this::merge, 1, 1,
                TimeUnit.MINUTES);
    }

    /**
     * Store a message in the instance.
     *
     * @param model
     *            the message to send.
     */
    public ChatMessageModel sendMessage(final ChatMessageModel model,
            final User user) {
        Preconditions.checkNotNull(model);
        verifyNonSpamming(user);

        updateAndSetModel(model, user);

        ChatMessageInstance chatMessage = new ChatMessageInstance(user.getId(),
                model);
        messages.addLast(chatMessage);

        if (messages.size() > MAXIMAL_NUMBER_OF_CHAT_MESSAGES) {
            messages.removeFirst();
        }

        hasChanged.set(true);
        log.info("Sending message {} in room {}", chatMessage, this);
        return model;
    }

    private void verifyNonSpamming(final User user) {
        // Our system is allowed to send messages
        if (user.getId().equals(1)) {
            return;
        }

        final long currentTime = System.currentTimeMillis();

        if (messages
                .stream()
                .filter((message) -> {
                    return message.getUserId() == user.getId()
                            && message.getTimestamp()
                                    + TimeUnit.SECONDS.toMillis(MESSAGE_FLOODING_TIMEOUT) > currentTime;
                }).count() > MESSAGE_FLOODING_MESSAGE_AMOUNT) {
            throw new IllegalArgumentException(String.format(
                    "You can not post %d messages within %d seconds",
                    MESSAGE_FLOODING_MESSAGE_AMOUNT, MESSAGE_FLOODING_TIMEOUT));
        }
    }

    private void updateAndSetModel(final ChatMessageModel model, final User user) {
        model.setId(chatMessageIdGenerator.generateId());
        model.setTimestamp(System.currentTimeMillis());
        model.setAuthor(user.getName());
        model.setMessage(profanityChecker.clearProfanity(model.getMessage()));
    }

    /**
     * Merge the changes of the instance in the database.
     */
    protected Future<?> merge() {
        if (hasChanged.getAndSet(false)) {
            log.info("Merging changes in room {}", this.getId());
            return interactWithRoom(instance -> {
                instance.persistMessages(messages);
                instance.merge();
            });
        }
        return interactWithRoom(RoomInstanceInUnitOfWork::merge);
    }

    /**
     * The cached messages in order to speed up retrieval.
     *
     * @return The latest {@link #MAXIMAL_NUMBER_OF_CHAT_MESSAGES} messages.
     */
    public List<ChatMessageModel> getMessages() {
        return this.messages.stream().map(ChatMessageInstance::transform)
                .collect(Collectors.toList());
    }

    /**
     * Get the instance's current song.
     *
     * @return the current song.
     */
    public Song getCurrentSong() {
        return this.currentSong.get().getSong();
    }

    /**
     * Get the progress of the current song.
     *
     * @return the progress in seconds.
     */
    public long getCurrentTime() {
        return this.currentSong.get().getTime();
    }

    /**
     * Add a vote.
     *
     * @param user
     *            User that votes.
     * @param valueOf
     *            Vote value.
     */
    public void addVote(final User user, final Vote valueOf) {
        if (this.votes.containsKey(user)) {
            throw new IllegalArgumentException("User should only vote once!");
        }
        this.votes.put(user, valueOf);
    }

    /**
     * Interact with a {@link RoomInstanceInUnitOfWork}.
     */
    @FunctionalInterface
    interface RoomInstanceInUnitOfWorkHandler {

        /**
         * Interact with the {@link RoomInstanceInUnitOfWork} in a {@code UnitOfWork}.
         *
         * @param roomInstance
         *            {@code RoomInstanceInUnitOfWork} to work with.
         */
        @RunInUnitOfWork
        void handle(RoomInstanceInUnitOfWork roomInstance);

    }
}
