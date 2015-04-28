package me.moodcat.utils.network;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class UrlStreamFactory {

    /**
     * Create a InputStream object according to stream retrieved from the {@link URL} retrieved from
     * the uri.
     *
     * @param uri
     *            The name of the url to retrieve.
     * @return A stream object which represents the response of the url.
     * @throws IOException
     *             if an I/O exception occurs.
     */
    public String getContent(final String uri) throws IOException {
        return IOUtils.toString(new URL(uri).openStream());
    }

}
