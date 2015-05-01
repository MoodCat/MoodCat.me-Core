package me.moodcat.core;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import me.moodcat.api.RootApi;
import me.moodcat.api.SongAPI;
import me.moodcat.database.MockData;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;

import datastructures.dataholders.Data;

public class App {

    private static final int SERVER_PORT = 8080;

    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(final String[] args) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        final App app = new App();
        app.startServer();
        app.joinThread();
    }

    private final Server server;

    public App() {
        final File staticsFolder = new File("src/main/resources/static");

        for (final String file : staticsFolder.list()) {
            log.info("Found resouce {}", file);
        }

        this.server = new Server(SERVER_PORT);
        this.server.setSessionIdManager(new HashSessionIdManager());
        this.server.setHandler(this.attachHandlers(staticsFolder));
    }

    private ContextHandlerCollection attachHandlers(final File staticsFolder) {
        final MoodcatHandler moodcatHandler = new MoodcatHandler(staticsFolder);

        final ResourceHandler resources = new ResourceHandler();
        resources.setBaseResource(Resource.newResource(staticsFolder));
        resources.setDirectoriesListed(false);
        resources.setCacheControl("max-age=3600");

        final HashSessionManager hashSessionManager = new HashSessionManager();
        hashSessionManager.setMaxInactiveInterval(1800);

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
    private void startServer() throws Exception {
        this.server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
    }

    /**
     * Joins the {@link App} server.
     */
    private void joinThread() throws InterruptedException {
        this.server.join();
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

    /**
     * The MoodcatHandler functions as an entry point for the Moodcat API.
     * Its quite a standard ServletContextHandler, but it adds initializes three things:
     * <ul>
     * <li>Initializing a GuiceResteasyBootstrapServletContextListener, which is used to handle
     * requests through Resteasy in combination with Google Guice dependency injection</li>
     * <li>Adding a Guice requiest Filter for Guice servlet tools</li>
     * <li>Adding the HttpServletDispatcher which dispatches the incoming requests through the set
     * up filters and listeners</li>
     * </ul>
     */
    public class MoodcatHandler extends ServletContextHandler {

        public MoodcatHandler(final File rootFolder, final Module... overrides) {
            this.addEventListener(new GuiceResteasyBootstrapServletContextListener() {

                @Override
                protected List<Module> getModules(final ServletContext context) {
                    final MoodcatServletModule module = new MoodcatServletModule(rootFolder);
                    return ImmutableList.<Module> of(Modules.override(module).with(overrides));
                }

                @Override
                protected void withInjector(final Injector injector) {
                    final FilterHolder guiceFilterHolder = new FilterHolder(
                            injector.getInstance(GuiceFilter.class));
                    MoodcatHandler.this.addFilter(guiceFilterHolder, "/*",
                            EnumSet.allOf(DispatcherType.class));
                }
            });

            this.addServlet(HttpServletDispatcher.class, "/");
        }

    }

    /**
     * The MoodcatServletModule is the Dependency Injection module for the
     * MoodCat base API service. It tells Google Guice which classes (and their
     * dependencies) to instantiate.
     */
    public class MoodcatServletModule extends ServletModule {

        private final File rootFolder;

        public MoodcatServletModule(final File rootFolder) {
            this.rootFolder = rootFolder;
        }

        @Override
        protected void configureServlets() {
            // Install the JaxrsModule to use Guice with Jax RS
            this.install(new JaxrsModule());
            // Ensure an ObjectMapper (json (de)serializer) is available at this point
            this.requireBinding(ObjectMapper.class);
            // Provide a way to access the resources folder from other classes
            this.bind(File.class).annotatedWith(Names.named("root.folder"))
                    .toInstance(this.rootFolder);
            // Bind the database module
            this.install(new JpaPersistModule("moodcat"));
            this.requireBinding(EntityManager.class);
            this.requireBinding(EntityManagerFactory.class);
            // Insert mock data
            this.bind(MockData.class).asEagerSingleton();
            // Bind the reqources, so they can serve requests
            this.bind(RootApi.class);
            this.bind(SongAPI.class);
        }
    }

    // Verify Recommendation library is loaded.
    // TODO: DELETE ME
    public class A implements Data<A> {

        @Override
        public int compareTo(final A arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public A copy() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getIndex() {
            // TODO Auto-generated method stub
            return 0;
        }

    }

}
