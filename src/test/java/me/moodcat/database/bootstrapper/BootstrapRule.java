package me.moodcat.database.bootstrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.moodcat.database.entities.Artist;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link MethodRule} that ensures a certain database environment is loaded
 * for the test.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class BootstrapRule implements TestRule {

    private final Bootstrapper bootstrapper;

    @Inject
    public BootstrapRule(Bootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        final TestBootstrap testBootstrapAnnotation =
                description.getAnnotation(TestBootstrap.class);

        if (testBootstrapAnnotation == null) {
            return statement;
        }

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    for(String configuration : testBootstrapAnnotation.value()) {
                        bootstrapper.parseFromResource(configuration);
                    }
                    statement.evaluate();
                }
                finally {
                    bootstrapper.cleanup();
                }
            }

        };
    }

    /**
     * Get an artist.
     *
     * @return an artist
     */
    public Artist getFirstArtist() {
        return bootstrapper.getFirstArtist();
    }

}
