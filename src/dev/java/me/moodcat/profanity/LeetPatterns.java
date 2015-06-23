package me.moodcat.profanity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool to create regex patterns to also match leet profanity.
 */
public class LeetPatterns {

    final static String LIST_DIRECTORY = "./src/main/resources/profanity/";

    public static void main(String[] args) throws IOException {
        LeetPatterns patterns = new LeetPatterns();
        patterns.createLeetPatternList("nl");
        patterns.createLeetPatternList("en");
        patterns.mergeFiles("patterns.txt", "nl-patterns.txt", "en-patterns.txt");
    }

    public void createLeetPatternList(String language) throws IOException {
        String path = LIST_DIRECTORY + language + ".txt";
        String[] words = new String(Files.readAllBytes(Paths.get(path))).split("\n");
        List<String> leetWords = new ArrayList<>();
        for (String word : words) {
            leetWords.add(leetReplace(word));
        }
        writeList(leetWords, language + "-patterns.txt");
    }

    private void writeList(List<String> words, String fileName) throws IOException {
        if (words.isEmpty())
            return;
        String path = LIST_DIRECTORY + fileName;
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(word);
            builder.append('\n');
        }
        builder.deleteCharAt(builder.length() - 1);
        Files.write(Paths.get(path), builder.toString().getBytes());
    }

    private String leetReplace(String word) {
        String leetWord = word.toLowerCase();
        leetWord = leetWord.replaceAll("o", "[o0]");
        leetWord = leetWord.replaceAll("a", "[a@4]");
        leetWord = leetWord.replaceAll("i", "[i1|]");
        leetWord = leetWord.replaceAll("t", "[t7]");
        leetWord = leetWord.replaceAll("e", "[e3]");
        leetWord = leetWord.replaceAll("g", "[g9]");
        leetWord = leetWord.replaceAll("s", "[s5]");
        return leetWord;
    }

    public void mergeFiles(String outFile, String... files) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (String file : files) {
            String path = LIST_DIRECTORY + file;
            builder.append(new String(Files.readAllBytes(Paths.get(path))));
            builder.append('\n');
        }
        builder.deleteCharAt(builder.length() - 1);
        Files.write(Paths.get(LIST_DIRECTORY + outFile), builder.toString().getBytes());
    }
}
