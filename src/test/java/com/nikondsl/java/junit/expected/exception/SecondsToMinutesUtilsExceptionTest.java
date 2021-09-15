package com.nikondsl.java.junit.expected.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.nikondsl.utils.LoggingExtension;
import com.nikondsl.utils.HideExceptionLogging;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoggingExtension.class)
public class SecondsToMinutesUtilsExceptionTest {

    @HideExceptionLogging({NumberFormatException.class, IllegalArgumentException.class, NullPointerException.class})
    private static SecondsToMinutesUtils secsToMins;

    @BeforeAll
    public static void setUp() throws Exception {
        secsToMins = new SecondsToMinutesUtils();
    }

    @Test
    public void testSecsToMinsHappyPath() {
        int seconds = 65;
        assertEquals(1, secsToMins.secsToMins(seconds));
    }

    @Test
    public void testNewStyleSecsToMinsException() {
        int seconds = -1;
        //hide an exception, so it will not be shown in logs
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

    @Test
    public void testOldStyleSecsToMinsException() {
        int seconds = -1;
        try {
            secsToMins.secsToMins(seconds);
            fail();
        } catch (Exception ex) {
            //hide an exception, so it will not be shown in logs
        }
    }

    @Test
    public void testSecsToMinsStringException() {
        String seconds = "-1";
        //hide an exception, so it will not be shown in logs
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

    @Test
    public void testSecsToMinsStringExceptionParse() {
        String seconds = "abc";
        //hide an exception, so it will not be shown in logs
        assertThrows(IllegalArgumentException.class, () -> secsToMins.secsToMins(seconds));
    }

    @Test
    public void testSecsToMinsStringNotExpectedException() {
        //here is NPE expected, but we do not hide it, so it will be shown in logs
        assertThrows(NullPointerException.class, () -> secsToMins.secsToMins((String) null));
    }
}
