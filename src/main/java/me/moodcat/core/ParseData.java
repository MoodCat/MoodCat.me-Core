package me.moodcat.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class to parse the features from the EEMCS data and ouput a .JSON file with only the features
 * we are using.
 */
public class ParseData {

    /**
     * The objectMapper that maps the old file into the new file.
     */
    private ObjectMapper objectMapper;

    /**
     * The data class where we store the data.
     */
    private JsonData result;

    /**
     * This class makes a new objectMapper wich mapps the JSOn objects.
     */
    public ParseData() {

        objectMapper = new ObjectMapper();

    }

    /**
     * Get the latest result class.
     * 
     * @return The JsonData class result.
     */
    public JsonData getResult() {
        return result;

    }

    /**
     * A method to parse the data and ouput the data to work with it.
     * 
     * @param input
     *            - the path to the input file
     * @param output
     *            - the path where to output the file.
     * @return A json data class to store the data.
     */
    public JsonData parseFile(String input, String output) {
        try (InputStream in = new FileInputStream(input)) {
            result = objectMapper.readValue(in, JsonData.class);

            objectMapper.writeValue(new File(output), result);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Parse features from a whole map.
     * 
     * @param inputFolder
     *            - The folder where the features are located,should end with /.
     * @param outputPath
     *            - The folder where the new files need to be stored.
     */
    public void parseMap(String inputFolder, String outputPath) {
        try {
            java.util.Iterator<Path> iterator = Files.walk(Paths.get(inputFolder)).iterator();

            while (iterator.hasNext()) {
                Path filePath = iterator.next();
                if (Files.isRegularFile(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    int position = fileName.lastIndexOf(".");
                    if (position > 0) {
                        fileName.substring(0, position);
                    }

                    parseFile(inputFolder + filePath.getFileName().toString(),
                            outputPath + fileName + ".json");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
