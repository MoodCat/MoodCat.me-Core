package me.moodcat.backend.rooms;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.database.DatabaseTestModule;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * Created by jwgmeligmeyling on 9-6-15.
 */
@RunWith(JukitoRunner.class)
@UseModules(InjectionTest.TestModule.class)
public class InjectionTest {

    private static LifeCycle lifeCycle = new AbstractLifeCycle() {
        @Override
        protected void doStart() throws Exception {
            super.doStart();
        }
    };

    @BeforeClass
    public static void beforeClass() throws Exception {
        lifeCycle.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        lifeCycle.stop();
    }

    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            this.install(new DatabaseTestModule());
            this.bindConstant().annotatedWith(Names.named("thread.pool.size")).to(4);
            this.bind(LifeCycle.class).toInstance(lifeCycle);

            install(new FactoryModuleBuilder()
                    .build(SongInstanceFactory.class));

            install(new FactoryModuleBuilder()
                    .build(RoomInstanceFactory.class));

            this.bind(UnitOfWorkSchedulingService.class).asEagerSingleton();
        }
    }

    @Inject
    private Injector injector;

    @Test
    public void testRoomInstanceFactory() {
        RoomInstanceFactory songInstanceFactory = injector.getInstance(RoomInstanceFactory.class);
        Assert.assertNotNull(songInstanceFactory);
    }

    @Test
    public void testSongInstanceFactory() {
        SongInstanceFactory songInstanceFactory = injector.getInstance(SongInstanceFactory.class);
        Assert.assertNotNull(songInstanceFactory);
    }

    @Test
    public void testBindUOWSChedulingService(){
        UnitOfWorkSchedulingService songInstanceFactory = injector.getInstance(UnitOfWorkSchedulingService.class);
        Assert.assertNotNull(songInstanceFactory);

    }
}
