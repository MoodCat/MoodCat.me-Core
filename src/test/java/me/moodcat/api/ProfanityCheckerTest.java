package me.moodcat.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaap Heijligers
 */
public class ProfanityCheckerTest {

    final static String PROFANITY_WORD = "fuck";

    final static String PROFANITY_WORD_CENSORED = "****";

    final static String PROFANITY_STRING = "leopard pig shit eagle";

    final static String PROFANITY_STRING_CENSORED = "leopard pig **** eagle";

    final static String PROFANITY_CAPITALS = "sHiT";

    final static String PROFANITY_CAPITALS_CENSORED = "****";

    final static String PROFANITY_LEET = "5h17";

    final static String PROFANITY_LEET_CENSORED = "****";

    private ProfanityChecker profanityChecker;

    @Before
    public void before() {
        profanityChecker = new ProfanityChecker();
    }

    @Test
    public void testProfanityWord() {
        assertEquals(PROFANITY_WORD_CENSORED, profanityChecker.clearProfanity(PROFANITY_WORD));
    }

    @Test
    public void testProfanityString() {
        assertEquals(PROFANITY_STRING_CENSORED, profanityChecker.clearProfanity(PROFANITY_STRING));
    }

    @Test
    public void testProfanityCapitals() {
        assertEquals(PROFANITY_CAPITALS_CENSORED, profanityChecker.clearProfanity(PROFANITY_CAPITALS));
    }

    @Test
    public void testProfanityLeet() {
        assertEquals(PROFANITY_LEET_CENSORED, profanityChecker.clearProfanity(PROFANITY_LEET));
    }

}
