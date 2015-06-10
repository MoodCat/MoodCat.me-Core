package me.moodcat.backend.mocks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.rooms.ChatMessageFactory;
import me.moodcat.backend.rooms.RoomInstance;
import me.moodcat.backend.rooms.RoomInstanceFactory;
import me.moodcat.backend.rooms.SongInstanceFactory;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.Room;

/**
 * Created by jwgmeligmeyling on 9-6-15.
 */
public class RoomInstanceFactoryMock implements RoomInstanceFactory {

    private final SongInstanceFactory songInstanceFactory;
    private final Provider<RoomDAO> roomDAOProvider;
    private final ChatMessageFactory chatMessageFactory;
    private final UnitOfWorkSchedulingService unitOfWorkSchedulingService;

    @Inject
    public RoomInstanceFactoryMock(final Provider<SongDAO> songDAOProvider,
                                   final Provider<RoomDAO> roomDAOProvider,
                                   final ChatMessageFactory chatMessageFactory,
                                   final UnitOfWorkSchedulingService unitOfWorkSchedulingService) {
        this.songInstanceFactory = new SongInstanceFactoryMock(songDAOProvider);
        this.roomDAOProvider = roomDAOProvider;
        this.chatMessageFactory = chatMessageFactory;
        this.unitOfWorkSchedulingService = unitOfWorkSchedulingService;
    }

    @Override
    public RoomInstance create(final Room room) {
        return new RoomInstance(songInstanceFactory, roomDAOProvider,
            unitOfWorkSchedulingService, chatMessageFactory, room);
    }
}
