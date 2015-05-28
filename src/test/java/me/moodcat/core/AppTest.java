package me.moodcat.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for App.
 */
public class AppTest {

    private static App app;

    @BeforeClass
    public static void startServer() throws Exception {
        app = new App();
        app.startServer();
    }

    @AfterClass
    public static void stopServer() throws InterruptedException {
        app.stopServer();
        app.joinThread();
    }

    @Test
    public void isRunningTest() {
        assertTrue(app.server.isRunning());
    }

    @Test
    public void testHandlerStarted() {
        final Handler handler = app.server.getHandler();
        assertTrue(handler.isStarted());
    }

    @Test
    public void bindsMoodcatHandler() {
        final ContextHandlerCollection handlers = (ContextHandlerCollection) app.server
                .getHandler();
        for (final Handler handler : handlers.getChildHandlers()) {
            if (handler.getClass().equals(App.MoodcatHandler.class)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void bindsResourceHandler() {
        final ContextHandlerCollection handlers = (ContextHandlerCollection) app.server
                .getHandler();
        for (final Handler handler : handlers.getChildHandlers()) {
            if (handler.getClass().equals(ResourceHandler.class)) {
                return;
            }
        }
        fail();
    }

}
