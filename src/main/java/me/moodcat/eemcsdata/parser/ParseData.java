package me.moodcat.eemcsdata.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.moodcat.eemcsdata.AcousticBrainzData;

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
    private AcousticBrainzData result;

    /**
     * This class makes a new objectMapper wich mapps the JSOn objects.
     */
    public ParseData() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get the latest result class.
     *
     * @return The JsonData class result.
     */
    public AcousticBrainzData getResult() {
        return this.result;
    }

    /**
     * A method to parse the data from the local filesystem
     * and ouput the data to work with it.
     *
     * @param input
     *            - the path to the input file on the local file system.
     * @param output
     *            - the path where to output the file.
     * @return A json data class to store the data. Returns null if an I/O exception occurred.
     * @throws IOException
     *             If the file can not be found,throw IOException.
     */
    public AcousticBrainzData parseFileAsLocal(final String input, final String output)
            throws IOException {
        InputStream in = new FileInputStream(input);
        this.result = this.objectMapper.readValue(in, AcousticBrainzData.class);

        this.objectMapper.writeValue(new File(output), this.result);
        return this.result;

    }

    /**
     * A method to parse the data from a resource
     * and ouput the data to work with it.
     *
     * @param input
     *            - the path to the input file
     * @param output
     *            - the path where to output the file.
     * @return A json data class to store the data. Returns null if an I/O exception occurred.
     * @throws IOException
     *             If the file can not be found,throw IOException.
     */
    public AcousticBrainzData parseFileAsResource(final String input, final String output)
            throws IOException {
        InputStream in = AcousticBrainzData.class.getResourceAsStream(input);
        this.result = this.objectMapper.readValue(in, AcousticBrainzData.class);

        this.objectMapper.writeValue(new File(output), this.result);
        return this.result;

    }

    /**
     * Parse features from a whole map.
     *
     * @param inputFolder
     *            - The folder where the features are located,should end with /.
     * @param outputPath
     *            - The folder where the new files need to be stored.
     * @throws IOException
     *             If the folder can not be found,throw IOException.
     */
    public void parseFolder(final String inputFolder, final String outputPath,
            final boolean resource) throws IOException {
        final java.util.Iterator<Path> iterator = Files.walk(Paths.get(inputFolder)).iterator();

        while (iterator.hasNext()) {
            final Path filePath = iterator.next();

            if (Files.isRegularFile(filePath)) {

                this.parseFileAsLocal(inputFolder + filePath.getFileName().toString(),
                        outputPath + this.makeFileName(filePath) + ".json");
            }
        }
    }

    /**
     * Takes a FilePatha and extracts the filename.
     * 
     * @param filePath
     *            The path of the file.
     * @return
     *         the filename
     */
    public String makeFileName(Path filePath) {
        final String fileName = filePath.getFileName().toString();
        final int position = fileName.lastIndexOf(".");

        if (position > 0) {
            return fileName.substring(0, position);
        }

        return fileName;
    }
}
