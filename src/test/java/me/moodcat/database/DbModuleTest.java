package me.moodcat.database;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import me.moodcat.database.DbModule.DatabaseConfigurationException;

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

    @Test
    public void succesfullyLoadedConfiguration() throws IOException {
        when(module.getResourceStream(anyString())).thenReturn(
                new ByteArrayInputStream("javax.persistence.jdbc.password = bogus".getBytes()));

        module.configure(binder);

        verify(binder).install(any());
    }

    @Test
    public void succesfullyLoadedConfigurationWithEnvironmentVariable() throws IOException {
        when(module.getResourceStream(anyString())).thenReturn(
                new ByteArrayInputStream("".getBytes()));
        when(module.getSystemEnvironmentVariable()).thenReturn("yes");

        module.configure(binder);

        verify(binder).install(any());
    }

    @Test(expected = DatabaseConfigurationException.class)
    public void throwsExceptionWhenNoPasswordSet() throws IOException {
        when(module.getResourceStream(anyString())).thenReturn(
                new ByteArrayInputStream("".getBytes()));

        module.configure(binder);
    }
}
