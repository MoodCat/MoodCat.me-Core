package me.moodcat.database;

import java.io.IOException;
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
     * The name of the environment variable to set when running in production.
     */
    private static final String ENVIRONMENT_DATABASE_VARIABLE_NAME = "database-password";

    /**
     * The name of the property to set when running tests/locally.
     */
    private static final String PASSWORD_PROPERTY_NAME = "javax.persistence.jdbc.password";

    /**
     * Unit defined in src/main/resources/META-INF/persistence.xml.
     */
    private static final String MOODCAT_PERSISTENCE_UNIT = "moodcat";

    @Override
    @SneakyThrows
    protected void configure() {
        final JpaPersistModule jpaModule = new JpaPersistModule(MOODCAT_PERSISTENCE_UNIT);

        final Properties properties = getProperties();
        setDatabasePassword(properties);

        jpaModule.properties(properties);

        install(jpaModule);
    }

    protected Properties getProperties() throws IOException {
        try (InputStream stream = DbModule.class.getResourceAsStream("/persistence.properties")) {
            final Properties properties = new Properties();
            Preconditions.checkNotNull(stream, "Persistence properties not found");
            properties.load(stream);
            return properties;
        }

    }

    protected String getSystemEnvironmentVariable() {
        return System.getenv(ENVIRONMENT_DATABASE_VARIABLE_NAME);
    }

    private void setDatabasePassword(final Properties properties)
            throws DatabaseConfigurationException {
        final String databasePassword = getSystemEnvironmentVariable();

        // This will be null if it is run in the test environment.
        if (databasePassword != null) {
            properties.setProperty(PASSWORD_PROPERTY_NAME, databasePassword);
        }

        // Make sure the property is set, else notify developer/admin the configuration is invalid
        if (properties.getProperty(PASSWORD_PROPERTY_NAME) == null) {
            throw new DatabaseConfigurationException("The database password is not set."
                    + "Make sure the environment contains the variable '"
                    + ENVIRONMENT_DATABASE_VARIABLE_NAME + "'.");
        }
    }

    /**
     * Exception thrown when the database configuration is invalid.
     *
     * @author JeremybellEU
     */
    protected static class DatabaseConfigurationException extends Exception {

        /**
         * Generated UID.
         */
        private static final long serialVersionUID = 1071139882151857332L;

        private DatabaseConfigurationException(final String string) {
            super(string);
        }

    }
}
