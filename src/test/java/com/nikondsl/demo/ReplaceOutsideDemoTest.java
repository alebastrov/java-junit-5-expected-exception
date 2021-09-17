package com.nikondsl.demo;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.ClassesToWrapLoggers;
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
@ClassesToWrapLoggers({HoursHelper.class})
@HideByExceptionClassAndMessage({
        @ClassAndMessage(clazz = IllegalArgumentException.class, message = "Error: obvious"),
})
public class ReplaceOutsideDemoTest {

    private static HoursHelper<Double, Double> hoursHelper;

    @BeforeAll
    public static void setUp() throws Exception {
        hoursHelper = new HoursHelper<>();
    }

    @Test
    public void testRunFunctionHappyPath() {
        assertEquals(16.0, hoursHelper.process(x -> x * x, 4.0));
    }

    @Test
    public void testRunFunctionOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> hoursHelper.process(x -> { throw new IllegalArgumentException("Error: obvious"); }, 0.0)
        );
    }


}
