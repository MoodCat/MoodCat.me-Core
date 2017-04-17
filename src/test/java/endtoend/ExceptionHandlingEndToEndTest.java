package endtoend;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ExceptionHandlingEndToEndTest extends EndToEndTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    @Test
    public void error401IsCorrectlyHandled() {
        expected.expect(NotAuthorizedException.class);
        this.performGETRequest(Object.class, "users/me");
    }
    
    @Test
    public void error404IsCorrectlyHandled() {
        expected.expect(NotFoundException.class);
        this.performGETRequest(Object.class, "bogus");
    }

}
