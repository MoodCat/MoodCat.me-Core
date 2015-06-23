package me.moodcat.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Utility to match messages against a set of patterns to filter out profanity.
 */
public class ProfanityChecker {

    /**
     * The files with patterns of words to filter.
     */
    final static String PROFANITY_PATTERN_FILE = "./src/main/resources/profanity/patterns.txt";

    /**
     * The list of patterns.
     */
    private String[] profanityPatterns;

    /**
     * The profanity checker, reads the {@literal PROFANITY_PATTERN_FILE} on creation.
     */
    public ProfanityChecker() {
        try {
            profanityPatterns = new String(Files.readAllBytes(Paths
                    .get(PROFANITY_PATTERN_FILE))).split("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Matches each word of the message to the profanity list, replaces all instances of bad words
     * with stars.
     * 
     * @param message
     *            the message to filter.
     * @return the censored messaged.
     */
    public String clearProfanity(String message) {
        String[] words = message.split("\\s");
        for (String word : words) {
            for (String pattern : profanityPatterns) {
                if (word.toLowerCase().matches(pattern)) {
                    message = message.replaceAll(word, makeStars(word.length()));
                }
            }
        }
        return message;
    }

    /**
     * Create a '*' String of given length.
     * 
     * @param length
     *            the length of the string.
     * @return the string.
     */
    private String makeStars(int length) {
        char[] stars = new char[length];
        Arrays.fill(stars, '*');
        return new String(stars);
    }

}
