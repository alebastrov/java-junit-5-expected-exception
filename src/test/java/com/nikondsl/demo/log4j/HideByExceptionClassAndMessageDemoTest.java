package com.nikondsl.demo.log4j;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClassAndMessage;
import com.nikondsl.jupiter.logging.extension.LoggingExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class demonstrates how to hide ALL exceptions from logs if all of them are expected during testing.
 * Just run tests and see that logs does not contain any printed exception.
 */
@ExtendWith(LoggingExtension.class)
public class HideByExceptionClassAndMessageDemoTest {

    @HideByExceptionClassAndMessage({
            @ClassAndMessage(clazz = NumberFormatException.class, message = "For input string:"),
            @ClassAndMessage(clazz = IllegalArgumentException.class, message = "cannot be 0 or negative"),
            @ClassAndMessage(clazz = NullPointerException.class, message = "Argument cannot be null")
    })
    private static SecondsToMinutesUtils secsToMins;

    @BeforeAll
    public static void setUp() throws Exception {
        secsToMins = new SecondsToMinutesUtils();
    }

    @Test
    public void testSecsToMinsHappyPath() {
        int seconds = 185;
        assertEquals(3, secsToMins.secsToMins(seconds));
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
