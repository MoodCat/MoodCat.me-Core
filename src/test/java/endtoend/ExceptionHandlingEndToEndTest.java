package endtoend;

import java.util.Map;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;

import me.moodcat.api.SongAPI.ClassificationRequest;
import me.moodcat.api.models.ChatMessageModel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Maps;


public class ExceptionHandlingEndToEndTest extends EndToEndTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    @Test
    public void error404IsCorrectlyHandled() {
        expected.expect(NotFoundException.class);
        this.performGETRequest(Object.class, "bogus");
    }
    
    @Test
    public void error401IsCorrectlyHandled() {
        expected.expectMessage("HTTP 403 Forbidden");
        this.performGETRequest(Object.class, "users/me");
    }
    
    @Test
    public void error403IsCorrectlyHandled() {
        expected.expectMessage("HTTP 403 Forbidden");
        expected.expect(ForbiddenException.class);

        Map<String, Object> queryParams = Maps.newHashMap();
        queryParams.put("token", "user");
        
        ClassificationRequest request = new ClassificationRequest();
        request.setArousal(1.0);
        request.setValence(1.0);

        this.performPOSTRequestWithQueryParams(Object.class, "songs/add",
                Entity.json(request), queryParams);
    }

}
