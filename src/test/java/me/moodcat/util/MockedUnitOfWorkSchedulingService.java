package me.moodcat.util;

import me.moodcat.backend.UnitOfWorkSchedulingService;
import org.eclipse.jetty.util.component.LifeCycle;
import org.mockito.Mockito;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class MockedUnitOfWorkSchedulingService extends UnitOfWorkSchedulingService {

    public MockedUnitOfWorkSchedulingService() {
        super(null, 4, Mockito.mock(LifeCycle.class));
    }

    @Override
    public <T> Future<T> performInUnitOfWork(final Callable<T> callable) {
        return super.submit(callable);
    }

    @Override
    public Future<?> performInUnitOfWork(Runnable runnable) {
        return super.submit(runnable);
    }
}
