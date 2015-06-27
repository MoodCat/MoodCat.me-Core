package me.moodcat.backend;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import me.moodcat.util.CallableInUnitOfWork;
import me.moodcat.util.CallableInUnitOfWork.CallableInUnitOfWorkFactory;
import me.moodcat.util.DefaultLifceCycleListener;

import org.eclipse.jetty.util.component.LifeCycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * The UnitOfWorkSchedulingService can be used to schedule tasks in a
 * {@link com.google.inject.persist.UnitOfWork}.
 * Furthermore it listens on the current {@link LifeCycle} in order to terminate the threadpool on
 * shutdown.
 */
@Slf4j
@Singleton
public class UnitOfWorkSchedulingServiceImpl extends ScheduledThreadPoolExecutor implements
        UnitOfWorkSchedulingService, DefaultLifceCycleListener {

    private final CallableInUnitOfWork.CallableInUnitOfWorkFactory callableInUnitOfWorkFactory;

    @Inject
    public UnitOfWorkSchedulingServiceImpl(
            final CallableInUnitOfWorkFactory callableInUnitOfWorkFactory,
            @Named("thread.pool.size") final int corePoolSize,
            final LifeCycle lifeCycle) {
        super(corePoolSize);
        lifeCycle.addLifeCycleListener(this);
        this.callableInUnitOfWorkFactory = callableInUnitOfWorkFactory;
    }

    @Override
    public <T> Future<T> performInUnitOfWork(final Callable<T> callable) {
        final Callable<T> inUnitOfWork = callableInUnitOfWorkFactory.create(callable);
        return submit(inUnitOfWork);
    }

    @Override
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
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        log.info("Shutting down executor, waiting for {} tasks...", getActiveCount());
        this.shutdown();
    }

}
