package me.moodcat.util;

import junit.framework.TestCase;

import com.google.inject.persist.UnitOfWork;

/**
 * @author Jaap Heijligers
 */
public class CallableInUnitOfWorkTest extends TestCase {

    public void testCall() throws Exception {
        final String message = "TestMessage";
        final CallableInUnitOfWork<String> unitOfWork = new CallableInUnitOfWork<>(
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
