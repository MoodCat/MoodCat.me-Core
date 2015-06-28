package endtoend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.core.App;
import me.moodcat.database.bootstrapper.Bootstrapper;
import me.moodcat.soundcloud.SoundCloudIdentifier;
import me.moodcat.soundcloud.models.MeModel;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Matchers;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

public abstract class EndToEndTest {

    private static App app;

    private static SoundCloudIdentifier soundCloudIdentifier = mock(SoundCloudIdentifier.class);

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
        return ResteasyClientBuilder.newBuilder().build();
    }

    protected <T> T performGETRequest(final Class<T> clazz, final String endPoint) {
        return this.performRequest(endPoint, (response) -> response.get(clazz));
    }

    protected <T> T performGETRequestWithQueryParameters(final Class<T> clazz,
            final String endPoint,
            Map<String, Object> queryParameters) {
        return this.performRequestWithQueryParams(clazz, endPoint,
                (response) -> response.get(clazz), queryParameters);
    }

    protected <T> T performGETRequestWithGenericType(final GenericType<T> genericType,
            final String endPoint) {
        return this.performRequest(endPoint, (response) -> response.get(genericType));
    }

    protected <T> T performPOSTRequest(final Class<T> clazz,
            final String endPoint, final Entity<?> postEntity) {
        return this.performRequest(endPoint, (response) -> response.post(postEntity, clazz));
    }

    protected <T> T performPOSTRequestWithQueryParams(Class<T> clazz,
            String endPoint, Entity<T> postEntity, Map<String, Object> queryParameters) {
        return this.performRequestWithQueryParams(clazz, endPoint,
                (response) -> response.post(postEntity, clazz), queryParameters);
    }

    protected <T> T performRequestWithQueryParams(final Class<T> clazz,
            final String endPoint, final Function<Builder, T> callFunction,
            final Map<String, Object> queryParameters) {
        return this.performRequest(
                endPoint,
                callFunction,
                (target) -> {
                    WebTarget targetWithParams = target;

                    for (Entry<String, Object> entry : queryParameters.entrySet()) {
                        targetWithParams = targetWithParams.queryParam(entry.getKey(),
                                entry.getValue());
                    }

                    return targetWithParams;
                });
    }

    private <T> T performRequest(final String endPoint, final Function<Builder, T> callFunction) {
        return this.performRequest(endPoint, callFunction, (target) -> target);
    }

    private <T> T performRequest(final String endPoint, final Function<Builder, T> callFunction,
            final Function<WebTarget, WebTarget> applyQueryParams) {
        Client client = this.createClient();

        WebTarget target = client.target("http://localhost:8080/api");

        target = applyQueryParams.apply(target);

        Builder response = target.path(endPoint).request("application/json");

        T entity = callFunction.apply(response);
        client.close();
        return entity;
    }
}
