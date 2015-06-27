package me.moodcat.backend;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.ImplementedBy;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@ImplementedBy(UnitOfWorkSchedulingServiceImpl.class)
public interface UnitOfWorkSchedulingService extends ExecutorService, ScheduledExecutorService {

    /**
     * Perform a {@code Callable} in a {@link com.google.inject.persist.UnitOfWork} and
     * wait for its result.
     *
     * @param callable
     *            Callable to be ran
     * @param <T>
     *            Return type of the callable
     * @return Future that promises the return value of the callable
     */
    <T> Future<T> performInUnitOfWork(Callable<T> callable);

    /**
     * Perform a {@code Runnable} in a {@link com.google.inject.persist.UnitOfWork} and
     * wait for its result.
     *
     * @param runnable
     *            Runnable to run
     */
    Future<?> performInUnitOfWork(Runnable runnable);

}
