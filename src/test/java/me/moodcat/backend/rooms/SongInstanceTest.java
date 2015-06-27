package me.moodcat.backend.rooms;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import me.moodcat.backend.BackendTest;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Song;
import me.moodcat.util.JukitoRunnerSupportingMockAnnotations;
import me.moodcat.util.MockedUnitOfWorkSchedulingService;

import org.jukito.UseModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(JukitoRunnerSupportingMockAnnotations.class)
@UseModules(SongInstanceTest.SongInstanceTestModule.class)
public class SongInstanceTest extends BackendTest {

    private static SongDAO songDAO = Mockito.mock(SongDAO.class);

    public static class SongInstanceTestModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new RoomBackendModule());
            bind(SongDAO.class).toInstance(songDAO);
            bind(UnitOfWorkSchedulingService.class).to(MockedUnitOfWorkSchedulingService.class);
        }
    }

    private static final int DURATION = 1;

    @Spy
    private Song song = createSong();

    @Mock
    private SongInstance.StopObserver observer;

    @Inject
    private SongInstanceFactory songInstanceFactory;

    private SongInstance instance;

    @Inject
    private MockedUnitOfWorkSchedulingService unitOfWorkSchedulingService;

    @Before
    public void setUp() {
        when(song.getDuration()).thenReturn(DURATION);

        instance = songInstanceFactory.create(song);
        instance.addObserver(observer);
        verifyZeroInteractions(observer);
    }

    @Test
    public void canIncrementTime() throws InterruptedException {
        // Wait a little bit to trigger the changed.
        Thread.sleep(DURATION * 10);

        // Trigger changed
        instance.incrementTime();

        // And verify it is changed now
        instance.incrementTime();

        verify(observer).stopped();
        assertTrue(instance.getTime() > DURATION);
    }
    
    @Test
    public void canStop() {
        instance.stop();
        
        assertTrue(instance.isStopped());
    }
    
    @Test
    public void canOnlyStopOnce() {
        instance.stop();
        instance.stop();
        
        verify(observer).stopped();
    }

    @After
    public void tearDown() {
        unitOfWorkSchedulingService.shutdownNow();
    }

}
