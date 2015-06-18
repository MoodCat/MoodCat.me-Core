package endtoend;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;

import me.moodcat.backend.rooms.RoomBackend;
import me.moodcat.core.App;
import me.moodcat.database.bootstrapper.Bootstrapper;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.inject.Injector;

public abstract class EndToEndTest {
    
    private static App app;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        app = new App();
        
        app.startServer();
        
        final Injector injector = app.getInjector();

        // Bootstrap the database
        final Bootstrapper bootstrappper = injector.getInstance(Bootstrapper.class);
        bootstrappper.parseFromResource("/bootstrap/fall-out-boy.json");

        // Init inserted rooms
        final RoomBackend roomBackend = injector.getInstance(RoomBackend.class);
        roomBackend.initializeRooms();
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
    
    protected <T> T performRequest(Class<T> clazz, String endPoint) {
        Client client = this.createClient();

        Builder response = client.target("http://localhost:8080/api").path(endPoint).request();
        
        T entity = response.get(clazz);
        client.close();
        return entity;
    }
}
