package me.moodcat.util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * A Callable that is ran in a {@link UnitOfWork}. Used for database interaction in threads outside
 * the servlet and thus not filtered by the {@code PersistFilter}. Assumes the PersistService to
 * be started.
 *
 * @param <V>
 *            return type of the unit of work.
 */
@Slf4j
public class CallableInUnitOfWork<V> implements Callable<V> {

    /**
     * The work provider of which the unit of work is done.
     */
    private final Provider<UnitOfWork> workProvider;

    /**
     * The callable which is executed when the work has been finished.
     */
    private final Callable<V> callable;

    @Inject
    public CallableInUnitOfWork(final Provider<UnitOfWork> workProvider,
            final Callable<V> callable) {
        this.workProvider = workProvider;
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        final UnitOfWork work = workProvider.get();
        try {
            work.begin();
            return callable.call();
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
            throw t;
        } finally {
            work.end();
        }
    }

    /**
     * Factory for {@link Callable Callables} that should be ran in a {@link UnitOfWork}.
     */
    public static class CallableInUnitOfWorkFactory {

        /**
         * The provider of the work.
         */
        private final Provider<UnitOfWork> workProvider;

        @Inject
        public CallableInUnitOfWorkFactory(final Provider<UnitOfWork> workProvider) {
            this.workProvider = workProvider;
        }

        /**
         * Create a {@link Callable} in {@link UnitOfWork}.
         *
         * @param callable
         *            {@code Callable} to run in {@code UnitOfWork}
         * @param <T>
         *            Type of {@code Callable}
         * @return the {@code CallableInUnitOfWork}
         */
        public <T> Callable<T> create(final Callable<T> callable) {
            return new CallableInUnitOfWork<>(workProvider, callable);
        }

    }

}
