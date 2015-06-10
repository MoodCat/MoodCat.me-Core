package me.moodcat.core;

import com.google.inject.Injector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main entry-point for the backend server. Initializes all {@link me.moodcat.api APIs}, starts the
 * {@link #server} and connects to the database.
 */
@Slf4j
public class App {

    /**
     * The time that sessions are kept in the cache.
     */
    private static final int SESSION_KEEP_ALIVE = 1800;

    /**
     * Default TCP port.
     */
    private static final int SERVER_PORT = 8080;

    /**
     * The server that handles the requests.
     */
    @Getter(value = AccessLevel.PACKAGE)
    private final Server server;

    /**
     * Reference for injector in order to have concurrent transactions to our database.
     */
    @Getter(value = AccessLevel.PACKAGE)
    private final AtomicReference<Injector> injectorAtomicReference = new AtomicReference<>();

    /**
     * Instantiates the server and adds handlers for the requests.
     *
     * @throws IOException
     *             If the statics folder threw an IOException.
     */
    public App() throws IOException {
        final File staticsFolder = new File("src/main/resources/static/app");

        // Make sure the folder is available, else we can't start the server.
        if (!staticsFolder.exists() && !staticsFolder.mkdir()) {
            throw new IOException("Static folder could not be initialized.");
        }

        for (final String file : staticsFolder.list()) {
            log.info("Found resource {}", file);
        }

        this.server = new Server(SERVER_PORT);
        this.server.setSessionIdManager(new HashSessionIdManager());
        this.server.setHandler(this.attachHandlers(staticsFolder));
    }

    /**
     * Starts the server and waits for the server to shut down.
     *
     * @param args
     *            None.
     * @throws Exception
     *             Thrown when a thread-exception occured.
     */
    public static void main(final String... args) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        final App app = new App();
        app.startServer();
        app.joinThread();
    }

    private ContextHandlerCollection attachHandlers(final File staticsFolder) {
        final MoodcatHandler moodcatHandler = new MoodcatHandler(this, staticsFolder);

        final ResourceHandler resources = new ResourceHandler();
        resources.setBaseResource(Resource.newResource(staticsFolder));
        resources.setDirectoriesListed(false);
        resources.setCacheControl("max-age=3600");

        final HashSessionManager hashSessionManager = new HashSessionManager();
        hashSessionManager.setMaxInactiveInterval(SESSION_KEEP_ALIVE);

        final ContextHandlerCollection handlers = new ContextHandlerCollection();
        // CHECKSTYLE:OFF
        handlers.addContext("/", "/").setHandler(resources);
        handlers.addContext("/", "/").setHandler(moodcatHandler);
        // CHECKSTYLE:ON

        return handlers;
    }

    /**
     * Starts the {@link App} server.
     *
     * @throws Exception
     *             In case the server could not be started.
     */
    public void startServer() throws Exception {
        this.server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
    }

    /**
     * Joins the {@link App} server.
     * 
     * @throws InterruptedException
     *             if the joined thread is interrupted
     *             before or during the merging.
     */
    public void joinThread() throws InterruptedException {
        this.server.join();
    }

    /**
     * Get the injector.
     *
     * @return the injector
     */
    public Injector getInjector() {
        return injectorAtomicReference.get();
    }

    /**
     * Stops the {@link App} server.
     */
    public void stopServer() {
        try {
            this.server.stop();
        } catch (final Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
