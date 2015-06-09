package me.moodcat.core;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.rooms.*;
import me.moodcat.database.DbModule;

import org.eclipse.jetty.util.component.LifeCycle;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;

/**
 * The MoodcatServletModule is the Dependency Injection module for the
 * MoodCat base API service. It tells Google Guice which classes (and their
 * dependencies) to instantiate.
 */
@Slf4j
public class MoodcatServletModule extends ServletModule {

    /**
	 * 
	 */
	private final App app;

	/**
     * The string representation of the api package.
     */
    private static final String API_PACKAGE_NAME = "me.moodcat.api";

    /**
     * The string representation of the api package.
     */
    private static final String MAPPER_PACKAGE_NAME = "me.moodcat.core.mappers";

    /**
     * The rootFolder that contains all resources.
     */
    private final File rootFolder;

    public MoodcatServletModule(App app, final File rootFolder) {
        this.app = app;
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
        this.bindDatabaseModule();
        this.bindAPI();
        this.bindExceptionMappers();

        this.bindConstant().annotatedWith(Names.named("thread.pool.size")).to(4);
        this.bind(LifeCycle.class).toInstance(this.app.getServer());

        install(new FactoryModuleBuilder().build(SongInstanceFactory.class));
        install(new FactoryModuleBuilder().build(RoomInstanceFactory.class));

        this.bind(UnitOfWorkSchedulingService.class).asEagerSingleton();
        this.bind(RoomBackend.class).asEagerSingleton();
    }

    private void bindDatabaseModule() {
        install(new DbModule());
        filter("/*").through(PersistFilter.class);
        requireBinding(EntityManager.class);
        requireBinding(EntityManagerFactory.class);
    }

    @SneakyThrows
    private void bindAPI() {
        final Reflections reflections = new Reflections(API_PACKAGE_NAME);

        for (final Class<?> clazz : reflections.getTypesAnnotatedWith(Path.class)) {
            this.bind(clazz);
            log.info("Registering resource {}", clazz);
        }
    }

    @SneakyThrows
    private void bindExceptionMappers() {
        final Reflections reflections = new Reflections(MAPPER_PACKAGE_NAME);

        for (final Class<?> clazz : reflections.getTypesAnnotatedWith(Provider.class)) {
            this.bind(clazz);
            log.info("Registering exception mapper {}", clazz);
        }
    }
}