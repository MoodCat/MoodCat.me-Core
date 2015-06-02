package me.moodcat.util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

import java.util.concurrent.Callable;

/**
 * A Callable that is ran in a {@link UnitOfWork}. Used for database interaction in threads outside
 * the servlet and thus not filtered by the {@code PersistFilter}. Assumes the PersistService to
 * be started.
 */
public class CallableInUnitOfWork<V> implements Callable<V> {

    private final Provider<UnitOfWork> workProvider;
    private final Callable<V> callable;

    @Inject
    public CallableInUnitOfWork(Provider<UnitOfWork> workProvider, Callable<V> callable) {
        this.workProvider = workProvider;
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        UnitOfWork work = workProvider.get();
        try {
            work.begin();
            return callable.call();
        }
        finally {
            work.end();
        }
    }

    /**
     * Factory for {@link Callable Callables} that should be ran in a {@link UnitOfWork}.
     */
    public static class CallableInUnitOfWorkFactory {

        private final Provider<UnitOfWork> workProvider;

        @Inject
        public CallableInUnitOfWorkFactory(Provider<UnitOfWork> workProvider) {
            this.workProvider = workProvider;
        }

        /**
         * Create a  {@link Callable} in {@link UnitOfWork}.
         *
         * @param callable {@code Callable} to run in {@code UnitOfWork}
         * @param <T> Type of {@code Callable}
         * @return the {@code CallableInUnitOfWork}
         */
        public <T> CallableInUnitOfWork<T> create(Callable<T> callable) {
            return new CallableInUnitOfWork<>(workProvider, callable);
        }

    }

}
