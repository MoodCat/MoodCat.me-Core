package me.moodcat.util;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import junit.framework.TestCase;

/**
 * @author Jaap Heijligers
 */
public class CallableInUnitOfWorkTest extends TestCase {

    public void testCall() throws Exception {
        final String message = "TestMessage";
        CallableInUnitOfWork<String> unitOfWork = new CallableInUnitOfWork<>(
                () -> new UnitOfWork() {
                    @Override
                    public void begin() {

                    }

                    @Override
                    public void end() {

                    }
                }, () -> message);

        assertEquals(message, unitOfWork.call());
    }
}
