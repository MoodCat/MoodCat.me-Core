package me.moodcat.database;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import me.moodcat.database.DbModule.DatabaseConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Binder;

@RunWith(MockitoJUnitRunner.class)
public class DbModuleTest {

    @Mock
    private Binder binder;

    @Spy
    private DbModule module;

    private Properties properties;

    @Before
    public void before() throws IOException {
        properties = new Properties();
        when(module.getProperties()).thenReturn(properties);
    }

    @Test
    public void succesfullyLoadedConfiguration() throws IOException {
        properties.setProperty("javax.persistence.jdbc.password", "bogus");

        module.configure(binder);

        verify(binder).install(any());
    }

    @Test
    public void succesfullyLoadedConfigurationWithEnvironmentVariable() throws IOException {
        when(module.getSystemEnvironmentVariable()).thenReturn("yes");

        module.configure(binder);

        verify(binder).install(any());
    }

    @Test(expected = DatabaseConfigurationException.class)
    public void throwsExceptionWhenNoPasswordSet() throws IOException {
        module.configure(binder);
    }
}
