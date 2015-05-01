package me.moodcat.eemcsdata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import me.moodcat.database.embeddables.AcousticBrainzData;
import me.moodcat.eemcsdata.parser.ParseData;

import org.junit.Before;
import org.junit.Test;

public class ParseDataTest {

    ParseData data;

    @Before
    public void before() {
        data = new ParseData();

    }

    @Test
    public void testReadFileLocal() throws IOException {

        AcousticBrainzData result = data.parseFileAsLocal(
                "src/test/resources/acousticbrainz/testData/folderTest/100019264.txt",
                "src/test/resources/acousticbrainz/testData/100019264.json");

        assertEquals("F", result.getTonal().getKeyKey());
        assertEquals("minor", result.getTonal().getKeyScale());
        assertEquals(0.523020684719, result.getTonal().getKeyStrength(), 1e-4);
        assertEquals(434.193115234, result.getTonal().getTuningFrequency(), 1e-4);
        assertEquals(0.478912621737, result.getLowlevel().getDissonance().getMean(), 1e-4);
        assertEquals(0.708070397377, result.getLowlevel().getAverageLoudness(), 1e-4);
        assertEquals(120.082946777, result.getRhythm().getBpm(), 1e-4);
    }

    @Test
    public void testReadFolderLocal() throws IOException {

        data.parseFolder("src/test/resources/acousticbrainz/testData/folderTest/",
                "src/test/resources/acousticbrainz/result/", false);
        AcousticBrainzData result = data.getResult();

        assertEquals("F", result.getTonal().getKeyKey());
        assertEquals("minor", result.getTonal().getKeyScale());
        assertEquals(0.523020684719, result.getTonal().getKeyStrength(), 1e-4);
        assertEquals(434.193115234, result.getTonal().getTuningFrequency(), 1e-4);
        assertEquals(0.478912621737, result.getLowlevel().getDissonance().getMean(), 1e-4);
        assertEquals(0.708070397377, result.getLowlevel().getAverageLoudness(), 1e-4);
        assertEquals(120.082946777, result.getRhythm().getBpm(), 1e-4);
    }

    @Test(expected = IOException.class)
    public void testReadNotExsitsingFileLocal() throws IOException {
        data.parseFileAsLocal(
                "src/test/resources/acousticbrainz/testData/folderTest/nonExcisting.txt",
                "src/test/resources/acousticbrainz/testData/nonExcisting.json");
    }

    @Test(expected = IOException.class)
    public void testReadNotExsitsingFolderLocal() throws IOException {
        data.parseFolder("src/test/resources/acousticbrainz/unknown/",
                "src/test/resources/acousticbrainz/result/", false);
    }

    @Test
    public void testReadFileResource() throws IOException {

        AcousticBrainzData result = data.parseFileAsResource(
                "/acousticbrainz/testData/folderTest/100019264.txt",
                "src/test/resources/acousticbrainz/result/100019264.json");

        assertEquals("F", result.getTonal().getKeyKey());
        assertEquals("minor", result.getTonal().getKeyScale());
        assertEquals(0.523020684719, result.getTonal().getKeyStrength(), 1e-4);
        assertEquals(434.193115234, result.getTonal().getTuningFrequency(), 1e-4);
        assertEquals(0.478912621737, result.getLowlevel().getDissonance().getMean(), 1e-4);
        assertEquals(0.708070397377, result.getLowlevel().getAverageLoudness(), 1e-4);
        assertEquals(120.082946777, result.getRhythm().getBpm(), 1e-4);
    }

    @Test(expected = IOException.class)
    public void testReadNotExsitsingFileResource() throws IOException {
        data.parseFileAsLocal(
                "/acousticbrainz/testData/folderTest/nonExcisting.txt",
                "src/test/resources/acousticbrainz/result/nonExcisting.json");
    }

}
