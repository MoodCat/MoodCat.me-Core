package me.moodcat.database;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;

/**
 * This app is a server that will use {@link BulkInsertData} to insert data from a given list of
 * SoundCloud ids into the database.
 * 
 * @author Jaap Heijligers
 */
@Slf4j
public class BulkInsertApp {

    /**
     * The port the bulk insert server runs on.
     */
    private static final int SERVER_PORT = 9001;

    /**
     * The port the bulk insert server runs on.
     */
    private static final int NUMBER_OF_ROOMS = 5;

    /**
     * Reference for injector in order to have concurrent transactions to our database.
     */
    private final AtomicReference<Injector> injectorAtomicReference = new AtomicReference<>();

    /**
     * The server.
     */
    private Server server;

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
        server = new Server(SERVER_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
        server.setHandler(attachHandlers());
        server.start();
        // Clear the database to avoid duplicates
        injectorAtomicReference.get().getInstance(BulkInsertData.class).clear();

        // Insert the data
        injectorAtomicReference.get().getInstance(BulkInsertData.class).insertData();

        // Insert rooms
        injectorAtomicReference.get().getInstance(BulkInsertData.class)
                .insertRandomRooms(NUMBER_OF_ROOMS);

        // When done, stop the server
        server.stop();
    }

    /**
     * Stops the server.
     */
    public void stopServer() {
        try {
            this.server.stop();
        } catch (final Exception e) {
            log.error("Couldn't stop server.", e);
        }
    }

    /**
     * Create the handlers of the server.
     * 
     * @return the handlers.
     */
    public ContextHandlerCollection attachHandlers() {
        final ContextHandlerCollection handlers = new ContextHandlerCollection();

        MoodcatHandler moodcatHandler = new MoodcatHandler();
        handlers.addContext("/ ", "/").setHandler(moodcatHandler);

        return handlers;
    }

    /**
     * The MoodcatServletModule is the Dependency Injection module for the
     * MoodCat base API service. It tells Google Guice which classes (and their
     * dependencies) to instantiate.
     */
    public static class MoodcatServletModule extends ServletModule {

        /**
         * The rootFolder that contains all resources.
         */

        public MoodcatServletModule() {
        }

        @Override
        protected void configureServlets() {
            // Bind the database module
            this.bindDatabaseModule();

            // Bind the bulk insertion
            this.bind(BulkInsertData.class).asEagerSingleton();
        }

        private void bindDatabaseModule() {
            install(new DbModule());

            filter("/*").through(PersistFilter.class);
            requireBinding(EntityManager.class);
            requireBinding(EntityManagerFactory.class);
        }
    }

    /**
     * The handler used for injecting the database module.
     */
    public class MoodcatHandler extends ServletContextHandler {

        /**
         * Constructor that takes the rootFolder and zero or more Modules to the listener.
         *
         * @param overrides
         *            Zero or more modules that are attached to the listener.
         */
        public MoodcatHandler(final Module... overrides) {
            this.addEventListener(new GuiceResteasyBootstrapServletContextListener() {

                @Override
                protected List<Module> getModules(final ServletContext context) {
                    final MoodcatServletModule module = new MoodcatServletModule();
                    return ImmutableList.of(Modules.override(module).with(overrides));
                }

                @Override
                protected void withInjector(final Injector injector) {
                    final FilterHolder guiceFilterHolder = new FilterHolder(
                            injector.getInstance(GuiceFilter.class));
                    MoodcatHandler.this.addFilter(guiceFilterHolder, "/*",
                            EnumSet.allOf(DispatcherType.class));
                    injectorAtomicReference.set(injector);
                }
            });
            this.addServlet(HttpServletDispatcher.class, "/");
        }

    }

}
