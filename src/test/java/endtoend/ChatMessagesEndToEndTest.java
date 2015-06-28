package endtoend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import me.moodcat.api.models.ChatMessageModel;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ChatMessagesEndToEndTest extends EndToEndTest {

    @Test
    public void canPostMessages() {
        ChatMessageModel message = postMessage("System", "hallo");

        assertEquals("hallo", message.getMessage());
        assertEquals("System", message.getAuthor());
        assertNotNull(message.getId());
        assertNotNull(message.getTimestamp());
    }

    @Test
    public void canPostMultipleMessagesIdIncreases() {
        ChatMessageModel message1 = postMessage("System", "Message 1");

        ChatMessageModel message2 = postMessage("System", "Message 2");

        assertEquals(-1, message1.getId().compareTo(message2.getId()));
    }

    @Test
    public void canRetrieveMessagesAfterPosting() {
        ChatMessageModel message1 = postMessage("System", "First message");

        ChatMessageModel message2 = postMessage("System", "Second message");

        List<ChatMessageModel> messages = this.perform(invocation -> invocation.path("rooms")
                .path("1").path("messages")
                .request()
                .get(new GenericType<List<ChatMessageModel>>() {
                }));

        Assert.assertThat(messages, Matchers.hasItems(message1, message2));
    }

    @Test
    public void canRetrieveMessagesAfterCertainMessage() {
        ChatMessageModel message1 = postMessage("System", "Second-last message");

        ChatMessageModel message2 = postMessage("System", "Last message");

        List<ChatMessageModel> messages = this.perform(invocation -> invocation.path("rooms")
                .path("1").path("messages").path(message1.getId().toString())
                .request()
                .get(new GenericType<List<ChatMessageModel>>() {
                }));

        Assert.assertThat(messages, Matchers.contains(message2));
    }

    private ChatMessageModel postMessage(final String author, final String contents) {
        ChatMessageModel postMessage = new ChatMessageModel();
        postMessage.setAuthor(author);
        postMessage.setMessage(contents);

        ChatMessageModel message = this.perform(invocation -> invocation.path("rooms")
                .path("1").path("messages")
                .queryParam("token", "asdf")
                .request()
                .post(Entity.json(postMessage), ChatMessageModel.class));

        return message;
    }

}
