package endtoend;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;

import me.moodcat.api.SongAPI.ClassificationRequest;

import org.junit.Test;

public class PointsEndToEndTest extends EndToEndTest {

    @Test
    public void canObtainPointsWhenClassifying() {
        Integer oldPoints = fetchPoints();

        ClassificationRequest request = new ClassificationRequest();
        request.setArousal(0.5);
        request.setValence(-0.5);

        this.perform(invocation -> invocation.path("songs")
                .path("202330997").path("classify")
                .queryParam("token", "asdf")
                .request()
                .post(Entity.json(request)));

        Integer newPoints = fetchPoints();
        assertTrue(oldPoints < newPoints);
    }

    private Integer fetchPoints() {
        return this.perform(invocation -> invocation.path("users").path("me")
                .path("points")
                .queryParam("token", "asdf")
                .request()
                .get(Integer.class));
    }

}
