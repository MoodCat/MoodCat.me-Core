package endtoend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.Client;

import lombok.SneakyThrows;
import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.core.App;
import me.moodcat.database.bootstrapper.Bootstrapper;
import me.moodcat.soundcloud.SoundCloudIdentifier;
import me.moodcat.soundcloud.models.MeModel;
import me.moodcat.util.InjectTestRule;
import me.moodcat.util.Invocation;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.mockito.Matchers;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

public abstract class EndToEndTest {

    private static final String LOCAL_API_ENDPOINT = "http://localhost:8080/api";

    private static App app;

    private static SoundCloudIdentifier soundCloudIdentifier = mock(SoundCloudIdentifier.class);

    @Rule
    public InjectTestRule injectTestRule = new InjectTestRule(app.getInjector());

    @BeforeClass
    public static void setUpClass() throws Exception {
        MeModel meModel = createMeModel();
        when(soundCloudIdentifier.getMe(Matchers.eq("asdf"))).thenReturn(meModel);

        app = new App(new AbstractModule() {

            @Override
            protected void configure() {
                bind(SoundCloudIdentifier.class).toInstance(soundCloudIdentifier);
            }

        });

        app.startServer();

        final Injector injector = app.getInjector();

        // Bootstrap the database
        final Bootstrapper bootstrappper = injector.getInstance(Bootstrapper.class);
        bootstrappper.parseFromResource("/bootstrap/fall-out-boy.json");

        // Init inserted rooms
        final RoomBackend roomBackend = injector.getInstance(RoomBackend.class);
        roomBackend.initializeRooms();
    }

    private static MeModel createMeModel() {
        MeModel meModel = new MeModel();
        meModel.setUsername("System");
        meModel.setFullName("System");
        meModel.setId(1337);
        return meModel;
    }

    @AfterClass
    public static void tearDownClass() {
        app.stopServer();
    }

    /**
     * Obtain a HTTP-client to start a request.
     *
     * @return The HTTP-client.
     */
    protected Client createClient() {
        return ResteasyClientBuilder.newBuilder()
                .build();
    }

    @SneakyThrows
    protected <T> T perform(final Invocation<T> invocation) {
        Client client = createClient();
        try {
            return invocation.perform(client.target(LOCAL_API_ENDPOINT));
        } finally {
            client.close();
        }
    }

}
