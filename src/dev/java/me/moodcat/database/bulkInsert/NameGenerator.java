package me.moodcat.database.bulkInsert;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Name generator that uses an online service to generate random names for rooms.
 */
public class NameGenerator {

    /**
     * The URL for th site.
     */
    final static String URL = "http://www.clannames.net/name-generator-ajax?m=genName&c=&ts=";

    /**
     * The URL to obtain a token.
     */
    final static String TOKEN_URL = "http://www.clannames.net/name-generator-ajax?m=token";

    /**
     * The required cookie.
     */
    private String cookie;

    /**
     * The timestamp along with the cookie.
     */
    private String timestamp;

    /**
     * The HTTP client.
     */
    private HttpClient client;

    /**
     * Creates a name generator, and initializes it.
     *
     * @throws IOException
     */
    public NameGenerator() throws IOException {
        client = new DefaultHttpClient();
        initialize();
    }

    /**
     * Initialize the generator by requesting a token cookie and timestamp.
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        HttpGet request = new HttpGet(TOKEN_URL);
        HttpResponse response = client.execute(request);
        cookie = response.getHeaders("Set-Cookie")[0].getValue();
        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntity().getContent(), writer);
        timestamp = writer.toString();
    }

    /**
     * Generate a random room name.
     *
     * @return the generated name.
     * @throws IOException
     */
    public String generate() throws IOException {
        HttpGet request = new HttpGet(URL + timestamp);

        request.addHeader(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.125 Safari/537.36");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("Cookie", cookie);
        HttpResponse response = client.execute(request);
        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntity().getContent(), writer);
        String name = writer.getBuffer().toString();
        if (name.contains("||")) {
            name = name.split("\\|\\|")[0];
        }
        name = name.replaceAll("<br \\\\>", "");
        return name;
    }
}
