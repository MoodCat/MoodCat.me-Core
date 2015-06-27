package me.moodcat.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import me.moodcat.backend.UnitOfWorkSchedulingService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class MockedUnitOfWorkSchedulingService extends ScheduledThreadPoolExecutor implements
        UnitOfWorkSchedulingService {

    @Inject
    public MockedUnitOfWorkSchedulingService() {
        super(4);
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
