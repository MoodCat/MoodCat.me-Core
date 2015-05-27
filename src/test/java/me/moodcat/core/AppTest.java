package me.moodcat.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

<<<<<<< HEAD
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
=======
import java.io.IOException;

>>>>>>> Various PMD errors fixed.
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
        Handler handler = app.server.getHandler();
        assertTrue(handler.isStarted());
    }

    @Test
    public void bindsMoodcatHandler() {
        ContextHandlerCollection handlers = (ContextHandlerCollection) app.server.getHandler();
        for (Handler handler : handlers.getChildHandlers()) {
            if (handler.getClass().equals(App.MoodcatHandler.class)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void bindsResourceHandler() {
        ContextHandlerCollection handlers = (ContextHandlerCollection) app.server.getHandler();
        for (Handler handler : handlers.getChildHandlers()) {
            if (handler.getClass().equals(ResourceHandler.class)) {
                return;
            }
        }
        fail();
    }

}
