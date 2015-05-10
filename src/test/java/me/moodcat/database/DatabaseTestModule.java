package me.moodcat.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;

/**
 * The DatabaseTestModule extends the {@link DbModule}, but also starts the {@link PersistService}
 * to allow
 * database interaction from the unit tests. (The PersistService is normally started automatically
 * by the servlet).
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class DatabaseTestModule extends DbModule {

    @Override
    protected void configure() {
        super.configure();
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
