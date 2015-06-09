package me.moodcat.core;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;

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

    /**
	 * 
	 */
	private final App app;

	/**
     * Constructor that takes the rootFolder and zero or more Modules to the listener.
     *
     * @param rootFolder
     *            The rootFolder system path.
     * @param overrides
     *            Zero or more modules that are attached to the listener.
     * @param app TODO
     */
    public MoodcatHandler(App app, final File rootFolder, final Module... overrides) {
        this.app = app;
		this.addEventListener(new AppContextListener(rootFolder, overrides));

        this.addServlet(HttpServletDispatcher.class, "/");
    }

    /**
     * Listener that connects the API's to the correct overriden module.
     */
    private final class AppContextListener extends
            GuiceResteasyBootstrapServletContextListener {

        /**
         * The folder to retrieve all resources from.
         */
        private final File rootFolder;

        /**
         * All modules that should be overriding path calls.
         */
        private final Module[] overrides;

        protected AppContextListener(final File rootFolder, final Module... overrides) {
            this.rootFolder = rootFolder;
            this.overrides = overrides;
        }

        @Override
        protected List<Module> getModules(final ServletContext context) {
            final MoodcatServletModule module = new MoodcatServletModule(MoodcatHandler.this.app, rootFolder);
            return ImmutableList.<Module> of(Modules.override(module).with(overrides));
        }

        @Override
        protected void withInjector(final Injector injector) {
            final FilterHolder guiceFilterHolder = new FilterHolder(
                    injector.getInstance(GuiceFilter.class));
            MoodcatHandler.this.addFilter(guiceFilterHolder, "/*",
                    EnumSet.allOf(DispatcherType.class));
            MoodcatHandler.this.app.getInjectorAtomicReference().set(injector);
        }
    }

}