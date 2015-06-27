package me.moodcat.backend.rooms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import me.moodcat.api.models.ChatMessageModel;
import junitx.extensions.EqualsHashCodeTestCase;

@RunWith(Enclosed.class)
public class ChatMessageInstanceTest {

    public static class ChatMessageInstanceEqualsTest extends EqualsHashCodeTestCase {

        public ChatMessageInstanceEqualsTest(String name) {
            super(name);
        }

        @Override
        protected Object createInstance() throws Exception {
            ChatMessageModel model = new ChatMessageModel();
            model.setAuthor("user1");
            model.setId(1);
            model.setMessage("hi");
            model.setTimestamp(System.currentTimeMillis());

            return new ChatMessageInstance(1, model);
        }

        @Override
        protected Object createNotEqualInstance() throws Exception {
            ChatMessageModel model = new ChatMessageModel();
            model.setAuthor("user1");
            model.setId(1);
            model.setMessage("hi too!");
            model.setTimestamp(System.currentTimeMillis());

            return new ChatMessageInstance(1, model);
        }
    }

    public static class ChatMessageInstanceCompareTest {

        @Test
        public void compareChatMessageInstanceChecksModels() {
            ChatMessageModel model1 = mock(ChatMessageModel.class);
            ChatMessageModel model2 = mock(ChatMessageModel.class);

            when(model1.compareTo(model2)).thenReturn(0);

            ChatMessageInstance instance = new ChatMessageInstance(1, model1);
            ChatMessageInstance otherInstance = new ChatMessageInstance(1, model2);

            assertEquals(0, instance.compareTo(otherInstance));
        }
    }
}
