package me.moodcat.soundcloud;

import java.io.IOException;

import me.moodcat.utils.network.UrlStreamFactory;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class SoundCloudAPIConnectorTest {

    protected static final String SONG2_ARTIST = "katfyr";

    protected static final String SONG2_TITLE_ID = "binary-original-mix";

    protected static final String SONG2_TITLE = "Binary (Original Mix)";

    protected static final String SONG_JSON_REPRESENTATION =
            new StringBuilder()
                    .append("{")
                    .append("\"id\":101712416,")
                    .append("\"title\":\"")
                    .append(SONG2_TITLE)
                    .append("\",")
                    .append("\"kind\":\"track\",")
                    .append("\"artwork_url\":\"https://i1.sndcdn.com/artworks\",")
                    .append("\"permalink_url\":\"http://soundcloud.com/katfyr/binary-original-mix\",")
                    .append("\"permalink\":\"binary-original-mix\",")
                    .append("\"duration\":283793,")
                    .append("\"downloadable\":false,")
                    .append("\"user\":{")
                    .append("\"id\":524576,")
                    .append("\"kind\":\"user\",")
                    .append("\"permalink\":\"katfyr\",")
                    .append("\"username\":\"KATFYR\",")
                    .append("\"last_modified\":\"2015/04/12 21:50:11 +0000\",")
                    .append("\"uri\":\"https://api.soundcloud.com/users/524576\",")
                    .append("\"permalink_url\":\"http://soundcloud.com/katfyr\",")
                    .append("\"avatar_url\":\"https://i1.sndcdn.com/avatars\"")
                    .append("}")
                    .append("}").toString();

    /**
     * Returns JSON-representations.
     */
    @Mock
    protected UrlStreamFactory factory;

    /**
     * Attach {@link #factory} to the connector.
     *
     * @param connector
     *            The connector that the factory should attach to.
     * @param defaultValue
     *            The default string that the factory should return.
     */
    public void setUp(final SoundCloudAPIConnector connector, final String defaultValue) {
        connector.setUrlFactory(this.factory);

        try {
            Mockito.when(this.factory.getContent(Matchers.anyString())).thenReturn(
                    defaultValue);
        } catch (final IOException e) {
            Assert.fail();
        }
    }

}
