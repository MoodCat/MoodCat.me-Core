package me.moodcat.util;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaap Heijligers
 */
public class CallableInUnitOfWorkFactoryTest {

    @Test
    public void testCreate() throws Exception {
        CallableInUnitOfWork.CallableInUnitOfWorkFactory factory = new CallableInUnitOfWork.CallableInUnitOfWorkFactory(
                new Provider<UnitOfWork>() {

                    @Override
                    public UnitOfWork get() {
                        return new UnitOfWork() {

                            @Override
                            public void begin() {

                            }

                            @Override
                            public void end() {

                            }
                        };
                    }
                });

        final String message = "Message";

        Callable<String> callable = factory.create(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return message;
            }
        });

        assertEquals(message, callable.call());
    }
}
