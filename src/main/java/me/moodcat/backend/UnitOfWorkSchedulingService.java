package me.moodcat.backend;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import me.moodcat.util.CallableInUnitOfWork;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;
import org.eclipse.jetty.util.component.LifeCycle;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The UnitOfWorkSchedulingService can be used to schedule tasks in a
 * {@link com.google.inject.persist.UnitOfWork}.
 * Furthermore it listens on the current {@link LifeCycle} in order to terminate the threadpool on
 * shutdown.
 */
@Slf4j
@Singleton
public class UnitOfWorkSchedulingService extends ScheduledThreadPoolExecutor implements
        LifeCycle.Listener {

    private final CallableInUnitOfWork.CallableInUnitOfWorkFactory callableInUnitOfWorkFactory;

    @Inject
    public UnitOfWorkSchedulingService(
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            @Named("thread.pool.size") final int corePoolSize,
            final LifeCycle lifeCycle) {
        super(corePoolSize);
        lifeCycle.addLifeCycleListener(this);
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
    }

    /**
     * Submit a {@link Callable} to be ran in a {@link com.google.inject.persist.UnitOfWork}.
     *
     * @param callable
     *            Callable to be ran
     * @param <T>
     *            Return type of the callable
     * @return Future that promises the return value of the callable
     */
    public <T> Future<T> submitInUnitOfWork(final Callable<T> callable) {
        final Callable<T> inUnitOfWork = callableInUnitOfWorkFactory.create(callable);
        return submit(inUnitOfWork);
    }

    /**
     * Perform a {@code Callable} in a {@link com.google.inject.persist.UnitOfWork} and
     * wait for its result.
     *
     * @param callable
     *            Callable to be ran
     * @param <T>
     *            Return type of the callable
     * @return return value of the callable
     */
    public <T> Future<T> performInUnitOfWork(final Callable<T> callable) {
        return submitInUnitOfWork(callable);
    }

    /**
     * Perform a {@code Runnable} in a {@link com.google.inject.persist.UnitOfWork} and
     * wait for its result.
     *
     * @param runnable
     *          Runnable to run
     */
    public Future<?> performInUnitOfWork(final Runnable runnable) {
        return performInUnitOfWork(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {
        log.info("Instantiated {} with {} threads", getClass(), getPoolSize());
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        // No op
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
        // No op
    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        log.info("Shutting down executor, waiting for {} tasks...", getActiveCount());
        this.shutdown();
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
        // No op
    }

}
