package com.roytuts.java.junit.expected.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.roytuts.utils.LoggingExtension;
import com.roytuts.utils.Mitigate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoggingExtension.class)
public class SecondsToMinutesUtilsExceptionTest {

    @Mitigate({NumberFormatException.class, IllegalArgumentException.class})
    private static SecondsToMinutesUtils secsToMins;

    @BeforeAll
    public static void setUp() throws Exception {
        secsToMins = new SecondsToMinutesUtils();
    }

    @Test
    public void testSecsToMins() {
        int seconds = 65;
        assertEquals(1, secsToMins.secsToMins(seconds));
    }

    @Test
    public void testNewStyleSecsToMinsException() {
        int seconds = -1;
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

    @Test
    public void testOldStyleSecsToMinsException() {
        int seconds = -1;
        try {
            secsToMins.secsToMins(seconds);
            fail();
        } catch (Exception ex) {
        }
    }

    @Test
    public void testSecsToMinsStringException() {
        String seconds = "-1";
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

    @Test
    public void testSecsToMinsStringExceptionParse() {
        String seconds = "abc";
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

}
