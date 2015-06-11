package me.moodcat.core;

import java.io.File;
import java.lang.annotation.Annotation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.api.filters.AuthorizationFilter;
import me.moodcat.backend.UnitOfWorkSchedulingService;
import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.backend.rooms.RoomInstanceFactory;
import me.moodcat.backend.rooms.SongInstanceFactory;
import me.moodcat.database.DbModule;

import me.moodcat.database.entities.User;
import org.eclipse.jetty.util.component.LifeCycle;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.FactoryModuleBuilder;
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

    private static final String API_PACKAGE_NAME = "me.moodcat.api";

    private static final String MAPPER_PACKAGE_NAME = "me.moodcat.core.mappers";

    private static final int THREAD_POOL_SIZE = 4;

    private final App app;

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
        this.bindConstants();
        // Bind the database module
        this.bindDatabaseModule();
        this.bindAPI();
        this.bindExceptionMappers();
        this.bindFactories();
        // Bind eager singletons
        this.bind(UnitOfWorkSchedulingService.class).asEagerSingleton();
        this.bind(RoomBackend.class).asEagerSingleton();
    }

    private void bindConstants() {
        // Provide a way to access the resources folder from other classes
        this.bind(File.class).annotatedWith(Names.named("root.folder"))
            .toInstance(this.rootFolder);
        this.bindConstant().annotatedWith(Names.named("thread.pool.size")).to(THREAD_POOL_SIZE);
        this.bind(LifeCycle.class).toInstance(this.app.getServer());
    }

    private void bindFactories() {
        this.install(new FactoryModuleBuilder().build(SongInstanceFactory.class));
        this.install(new FactoryModuleBuilder().build(RoomInstanceFactory.class));
    }

    private void bindDatabaseModule() {
        install(new DbModule());
        filter("/*").through(PersistFilter.class);
        requireBinding(EntityManager.class);
        requireBinding(EntityManagerFactory.class);
    }

    private void bindAPI() {
        this.bindClassesAnnotatedWithInPackage(API_PACKAGE_NAME, Path.class);
    }

    private void bindExceptionMappers() {
        this.bindClassesAnnotatedWithInPackage(MAPPER_PACKAGE_NAME, Provider.class);
        this.bind(AuthorizationFilter.class);
    }

    private void bindClassesAnnotatedWithInPackage(final String packageName,
                                                   final Class<? extends Annotation> annotation) {
        final Reflections reflections = new Reflections(packageName);

        for (final Class<?> clazz : reflections.getTypesAnnotatedWith(annotation)) {
            this.bind(clazz);
            log.info("Registering class {}", clazz);
        }
    }

    @Provides
    @Named("current.user")
    @RequestScoped
    public User provideCurrentUser() {
        throw new NotAuthorizedException("user id must be manually seeded");
    }
}
