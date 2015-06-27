package me.moodcat.util;

import com.google.inject.Injector;
import org.jukito.JukitoRunner;
import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;


/**
 * Calls {@link MockitoAnnotations#initMocks(Object)} on the test to run like {@link MockitoJUnitRunner} does,
 * so the test class doesn't need to do it itself in a {@literal @}Before annotated method to have
 * its {@literal @}Mock annotated fields provided with mocks.
 * @author jfrantzius
 *
 */
public class JukitoRunnerSupportingMockAnnotations extends JukitoRunner {

    public JukitoRunnerSupportingMockAnnotations(Class<?> klass) throws InitializationError, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(klass);
    }

    public JukitoRunnerSupportingMockAnnotations(Class<?> klass, Injector injector) throws InitializationError, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(klass, injector);
    }

    @Override
    protected Object createTest() throws Exception {
        Object result = super.createTest();
        MockitoAnnotations.initMocks(result);
        return result;
    }
}