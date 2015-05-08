package me.moodcat.database;

import java.io.InputStream;
import java.util.Properties;

import lombok.SneakyThrows;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

/**
 * Guice AbstractModule that installs the JpaPersistModule for the current Persistence unit.
 * The properties are loaded from the resource which allows a different configuration under test.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class DbModule extends AbstractModule {

    /**
     * Unit defined in src/main/resources/META-INF/persistence.xml.
     */
    private static final String MOODCAT_PERSISTENCE_UNIT = "moodcat";

    @Override
    @SneakyThrows
    protected void configure() {
        final JpaPersistModule jpaModule = new JpaPersistModule(MOODCAT_PERSISTENCE_UNIT);

        try (InputStream stream = DbModule.class.getResourceAsStream("/persistence.properties")) {
            Preconditions.checkNotNull(stream, "Persistence properties not found");
            final Properties properties = new Properties();
            properties.load(stream);

            setDatabasePassword(properties);

            jpaModule.properties(properties);
        }

        install(jpaModule);
    }

    private void setDatabasePassword(final Properties properties) {
        final String databasePassword = System.getenv("database-password");

        // This will be null if it is run in the test environment.
        if (databasePassword != null) {
            properties.setProperty("javax.persistence.jdbc.password", databasePassword);
        }
    }
}
