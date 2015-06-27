package me.moodcat.backend.rooms;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import me.moodcat.api.ProfanityChecker;
import me.moodcat.api.models.ChatMessageModel;
import me.moodcat.backend.BackendTest;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.RoomDAO;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.controllers.UserDAO;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import me.moodcat.database.entities.User;
import me.moodcat.util.JukitoRunnerSupportingMockAnnotations;
import me.moodcat.util.MockedUnitOfWorkSchedulingService;
import org.hamcrest.Matchers;
import org.jukito.UseModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JukitoRunnerSupportingMockAnnotations.class)
@UseModules(RoomInstanceTest.RoomInstanceTestModule.class)
public class RoomInstanceTest extends BackendTest {

    private static UserDAO userDAO = Mockito.mock(UserDAO.class);

    private static RoomDAO roomDAO = Mockito.mock(RoomDAO.class);

    private static SongDAO songDAO = Mockito.mock(SongDAO.class);

    public static class RoomInstanceTestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new RoomBackendModule());
            bind(SongDAO.class).toInstance(songDAO);
            bind(RoomDAO.class).toInstance(roomDAO);
            bind(UserDAO.class).toInstance(userDAO);
            bind(ProfanityChecker.class).toInstance(Mockito.mock(ProfanityChecker.class));
            bind(UnitOfWorkSchedulingService.class).to(MockedUnitOfWorkSchedulingService.class);
        }
    }

    private RoomInstance instance;

    @Inject
    private RoomInstanceFactory roomInstanceFactory;

    @Inject
    private MockedUnitOfWorkSchedulingService unitOfWorkSchedulingService;

    private Song song;

    private Room room;

    private User user;

    @Before
    public void setUp() {
        song = createSong(1);
        room = createRoom(song);
        user = createUser();

        when(roomDAO.findById(room.getId())).thenReturn(room);
        when(userDAO.findById(user.getId())).thenReturn(user);

        instance = roomInstanceFactory.create(room);
    }

    @After
    public void tearDown() {
        unitOfWorkSchedulingService.shutdownNow();
    }

    @Test
    public void whenTooManyMessagesRemoveOneFromList() {
        for (int i = 0; i < RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES + 1; i++) {
            ChatMessageModel model = new ChatMessageModel();
            model.setMessage(String.valueOf(i));

            instance.sendMessage(model, createUser());
        }

        assertThat(instance.getMessages(), Matchers.iterableWithSize(RoomInstance.MAXIMAL_NUMBER_OF_CHAT_MESSAGES));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenOneUserSendsMessagesTooFastThrowsException() {
        ChatMessageModel model = new ChatMessageModel();
        model.setMessage("Spam");

        User user = mock(User.class);
        when(user.getId()).thenReturn(1337);

        for (int i = 0; i < 6; i++) {
        instance.sendMessage(model, user);
        }
    }

    @Test
    public void testReplayHistoryOnNoResults() {
        when(songDAO.findForDistance(room.getVaVector(), RoomInstance.NUMBER_OF_SELECTED_SONGS))
            .thenReturn(Collections.emptyList());

        assertThat(room.getPlayHistory(), Matchers.empty());
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(song, room.getCurrentSong());

        instance.playNext();

        assertThat(room.getPlayHistory(), Matchers.empty());
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(song, room.getCurrentSong());
    }


    @Test
    public void testPlayNextSongFromResults() throws ExecutionException, InterruptedException {
        Song newSong = createSong();
        stubFindForDistance(room, newSong);

        assertThat(room.getPlayHistory(), Matchers.empty());
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(song, room.getCurrentSong());

        instance.playNext().get();
        instance.merge().get();

        assertThat(room.getPlayHistory(), Matchers.contains(song));
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(newSong, room.getCurrentSong());
    }

    @Test
    public void testScheduleResults() throws ExecutionException, InterruptedException {
        Song newSong = createSong(2);
        Song newOtherSong = createSong(3);
        stubFindForDistance(room, newSong, newOtherSong);

        assertThat(room.getPlayHistory(), Matchers.empty());
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(song, room.getCurrentSong());

        instance.playNext().get();
        instance.merge().get();

        assertThat(room.getPlayHistory(), Matchers.contains(song));
        assertThat(room.getPlayQueue(), Matchers.contains(newOtherSong));
        assertEquals(newSong, room.getCurrentSong());
    }

    @Test
    public void testLimitHistory() throws ExecutionException, InterruptedException {
        List<Song> history = ImmutableList.copyOf(Stream.generate(BackendTest::createSong)
            .limit(RoomInstance.NUMBER_OF_SELECTED_SONGS)
            .iterator());

        room.setPlayHistory(history);

        Song newSong = createSong();
        stubFindForDistance(room, newSong);

        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(song, room.getCurrentSong());

        instance.playNext().get();
        instance.merge().get();

        List<Song> expectedHistory = Stream
            .concat(history.stream().skip(1), Stream.of(song))
            .collect(Collectors.toList());


        assertEquals(expectedHistory, room.getPlayHistory());
        assertThat(room.getPlayQueue(), Matchers.empty());
        assertEquals(newSong, room.getCurrentSong());
    }

    private void stubFindForDistance(Room room, Song... songs) {
        when(songDAO.findNewSongsFor(room)).thenReturn(Lists.newArrayList(songs));
    }

}
