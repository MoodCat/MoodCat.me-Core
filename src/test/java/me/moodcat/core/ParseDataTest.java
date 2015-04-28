package me.moodcat.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ParseDataTest {

    ObjectMapper objectMapper;

    @Before
    public void before() {
        objectMapper = new ObjectMapper();

    }

    @Test
    public void testReadFile() {

        try( InputStream in = ParseDataTest.class.getResourceAsStream("/acousticbrainz/sample-data.json")) {
            BrainzResult result = objectMapper.readValue(in, BrainzResult.class);
            Assert.assertEquals("C#", result.getTonal().getKeyKey());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class BrainzResult {

        @JsonProperty("tonal")
        private Tonal tonal;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Tonal {

            @JsonProperty("key_key")
            private String keyKey;

        }
    }

}
