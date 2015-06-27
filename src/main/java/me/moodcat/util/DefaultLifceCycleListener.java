package me.moodcat.util;

import org.eclipse.jetty.util.component.LifeCycle;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface DefaultLifceCycleListener extends  LifeCycle.Listener {

    default void lifeCycleStarting(LifeCycle var1) {}

    default void lifeCycleStarted(LifeCycle var1) {}

    default void lifeCycleFailure(LifeCycle var1, Throwable var2) {}

    default void lifeCycleStopping(LifeCycle var1) {}

    default void lifeCycleStopped(LifeCycle var1) {}
}
