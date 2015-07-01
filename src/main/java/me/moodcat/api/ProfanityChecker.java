package me.moodcat.api;

import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Utility to match messages against a set of patterns to filter out profanity.
 */
@Singleton
public class ProfanityChecker {

    /**
     * The files with patterns of words to filter.
     */
    private static final String PROFANITY_PATTERN_FILE = "./src/main/resources/profanity/patterns.txt";

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
                    .get(PROFANITY_PATTERN_FILE)))
                .replaceAll("\\r", "").split("\n");
        } catch (final IOException e) {
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
    public String clearProfanity(final String message) {
        StringBuilder builder = new StringBuilder();
        
        String[] words = message.split("\\s");
        
        for (String word : words) {
            String actualWord = word.toLowerCase().replaceAll("\\W+", "");
            
            checkPatterns(word, actualWord, builder);
            
            builder.append(' ');
        }
        
        return builder.substring(0, builder.length() - 1).toString();
    }
    
    private void checkPatterns(final String word, final String actualWord, final StringBuilder builder) {
        for (String pattern : profanityPatterns) {
            if (actualWord.matches(pattern)) {
                builder.append(word.replaceAll("\\w+", makeStars(actualWord.length())));
                return;
            }
        }
        
        builder.append(word);
    }

    /**
     * Create a '*' String of given length.
     * 
     * @param length
     *            the length of the string.
     * @return the string.
     */
    private String makeStars(final int length) {
        char[] stars = new char[length];
        Arrays.fill(stars, '*');
        return new String(stars);
    }

}
