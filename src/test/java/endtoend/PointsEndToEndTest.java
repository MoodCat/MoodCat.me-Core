package endtoend;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.client.Entity;

import me.moodcat.api.SongAPI.ClassificationRequest;
import org.junit.Test;

import com.google.common.collect.Maps;

public class PointsEndToEndTest extends EndToEndTest {

    @Test
    public void canObtainPointsWhenClassifying() {
        Map<String, Object> queryParams = Maps.newHashMap();
        queryParams.put("token", "asdf");
        
        Integer oldPoints = this.performGETRequestWithQueryParameters(Integer.class, "users/me/points", queryParams);
        
        ClassificationRequest request = new ClassificationRequest();
        request.setArousal(0.5);
        request.setValence(-0.5);

        this.performPOSTRequestWithQueryParams(ClassificationRequest.class,
                "songs/202330997/classify",
                Entity.json(request), queryParams);
        
        Integer newPoints = this.performGETRequestWithQueryParameters(Integer.class, "users/me/points", queryParams);
        assertTrue(oldPoints < newPoints);
    }

}
