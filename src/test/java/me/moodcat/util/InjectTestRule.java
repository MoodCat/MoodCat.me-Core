package me.moodcat.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

public class InjectTestRule implements MethodRule {

    private final Injector injector;

    public InjectTestRule(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                injectFields(target);
                base.evaluate();
            }

        };
    }

    private void injectFields(final Object target) throws IllegalAccessException {
        Class<?> clasz = target.getClass();
        for (Field field : clasz.getDeclaredFields()) {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject != null) {
                field.setAccessible(true);
                Type type = field.getGenericType();
                field.set(target, injector.getInstance(Key.get(type)));
            }
        }
    }

}
