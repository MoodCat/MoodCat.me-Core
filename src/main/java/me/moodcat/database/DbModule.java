package me.moodcat.database;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class DbModule extends AbstractModule {

    private static final String MOODCAT_PERSISTENCE_UNIT = "moodcat";

    @Override
    @SneakyThrows
    protected void configure() {
        JpaPersistModule jpaModule = new JpaPersistModule(MOODCAT_PERSISTENCE_UNIT);

        try (InputStream stream = DbModule.class.getResourceAsStream("/persistence.properties")) {
            Preconditions.checkNotNull(stream, "Persistence properties not found");
            Properties properties = new Properties();
            properties.load(stream);
            jpaModule.properties(properties);
        }

        install(jpaModule);
    }

}
