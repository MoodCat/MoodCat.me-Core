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
        assertEquals(profanityChecker.clearProfanity(PROFANITY_WORD), PROFANITY_WORD_CENSORED);
    }

    @Test
    public void testProfanityString() {
        assertEquals(profanityChecker.clearProfanity(PROFANITY_STRING), PROFANITY_STRING_CENSORED);
    }

    @Test
    public void testProfanityCapitals() {
        assertEquals(profanityChecker.clearProfanity(PROFANITY_CAPITALS), PROFANITY_CAPITALS_CENSORED);
    }

    @Test
    public void testProfanityLeet() {
        assertEquals(profanityChecker.clearProfanity(PROFANITY_LEET), PROFANITY_LEET_CENSORED);
    }

}
