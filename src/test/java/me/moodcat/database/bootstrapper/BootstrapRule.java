package me.moodcat.database.bootstrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Room;
import me.moodcat.database.entities.Song;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * A JUnit {@link MethodRule} that ensures a certain database environment is loaded
 * for the test.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Singleton
public class BootstrapRule implements TestRule {

    private final Bootstrapper bootstrapper;
    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    @Inject
    public BootstrapRule(Bootstrapper bootstrapper, EntityManagerFactory entityManagerFactory, EntityManager entityManager) {
        this.bootstrapper = bootstrapper;
        this.entityManager = entityManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        final TestBootstrap testBootstrapAnnotation =
                description.getAnnotation(TestBootstrap.class);

        if (testBootstrapAnnotation == null) {
            return statement;
        }

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    dropSchema();

                    for(String configuration : testBootstrapAnnotation.value()) {
                        bootstrapper.parseFromResource(configuration);
                    }
                    statement.evaluate();
                }
                finally {
                    bootstrapper.cleanup();
                }
            }

        };
    }

    protected void dropSchema() throws ExecutionException, InterruptedException, IOException {
        log.info("Dropping schema!");
        Configuration config = new Configuration();
        Properties properties = new Properties();
        properties.putAll(entityManagerFactory.getProperties());

        config.setProperties(properties);
        entityManagerFactory.getMetamodel().getEntities()
            .stream()
            .map(EntityType::getJavaType)
            .forEach(config::addAnnotatedClass);

        EntityManagerImpl em = (EntityManagerImpl) entityManager;
        Session session = em.getSession();
        session.doWork(connection -> new SchemaExport(config, connection).create(false, true));
        entityManager.clear();
        log.info("Dropped schema successfully!");
    }

    public Artist getArtist(Integer id) {
        return bootstrapper.getArtist(id);
    }

    public Room getRoom(Integer id) {
        return bootstrapper.getRoom(id);
    }

    public Song getSong(Integer id) {
        return bootstrapper.getSong(id);
    }

}
