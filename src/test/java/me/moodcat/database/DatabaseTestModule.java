package me.moodcat.database;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class DatabaseTestModule extends AbstractModule {

    private static final String MOODCAT_PERSISTENCE_UNIT = "moodcat";

    @Override
    protected void configure() {
        install(new JpaPersistModule(MOODCAT_PERSISTENCE_UNIT));
        bind(JPAInitializer.class).asEagerSingleton();
    }

    @Singleton
    public static class JPAInitializer {

        @Inject
        public JPAInitializer(final PersistService service) {
            service.start();
        }

    }
}
