package me.moodcat.database.bulkInsert;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import me.moodcat.database.DbModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.PersistService;
import com.google.inject.servlet.ServletModule;

/**
 * This app is a server that will use {@link BulkInsertData} to insert data from a given list of
 * SoundCloud ids into the database.
 */
public class BulkInsertApp {

    /**
     * The port the bulk insert server runs on.
     */
    private static final int NUMBER_OF_ROOMS = 10;

    /**
     * The main method, will clear and fill the database.
     *
     * @param args
     *            None.
     * @throws Exception
     *             when database communication has failed.
     */
    public static void main(final String[] args) throws Exception {
        new BulkInsertApp().run();
    }

    /**
     * Run the bulk insertion.
     *
     * @throws Exception
     *             when the bulkinsertion has failed.
     */
    public void run() throws Exception {
        final Injector injector = Guice.createInjector(new BulkInsertServletModule());
        final PersistService persistService = injector.getInstance(PersistService.class);
        persistService.start();
        final BulkInsertData bulkInsertData = injector.getInstance(BulkInsertData.class);
        bulkInsertData.clear();
        bulkInsertData.insertData();
        bulkInsertData.insertRandomRooms(NUMBER_OF_ROOMS);
    }

    /**
     * The BulkInsertServletModule is the Dependency Injection module for the
     * MoodCat base API service. It tells Google Guice which classes (and their
     * dependencies) to instantiate.
     */
    public static class BulkInsertServletModule extends ServletModule {

        @Override
        protected void configureServlets() {
            this.bindDatabaseModule();
            this.bind(BulkInsertData.class).asEagerSingleton();
        }

        private void bindDatabaseModule() {
            install(new DbModule());

            filter("/*").through(PersistFilter.class);
            requireBinding(EntityManager.class);
            requireBinding(EntityManagerFactory.class);
        }
    }

}
