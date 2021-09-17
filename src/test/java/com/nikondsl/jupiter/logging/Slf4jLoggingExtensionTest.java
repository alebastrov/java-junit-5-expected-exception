package com.nikondsl.jupiter.logging;

import com.nikondsl.jupiter.logging.adapters.AbstractLoggerAdapter;
import com.nikondsl.jupiter.logging.adapters.impl.Slf4JLoggerAdapter;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClass;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionMessage;
import com.nikondsl.jupiter.logging.extension.LoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Slf4jLoggingExtensionTest {
    private Map<Object, Object> sanitized = new LinkedHashMap<>();

    private LoggingExtension loggingExtension = new LoggingExtension() {
        @Override
        protected AbstractLoggerAdapter createLoggerAdapter(Object logger) {
            return new Slf4JLoggerAdapter((Logger) logger) {
                protected Object sanitize(Object arg) {
                    Object result = super.sanitize(arg);
                    sanitized.put(arg, result);
                    return result;
                }
            };
        }
    };

    private static class ClassWhichMayThrowException {
        private static Logger LOGGER = LoggerFactory.getLogger(ClassWhichMayThrowException.class);

        public void doOperate(Exception toThrow) throws Exception {
            if (toThrow == null) {
                return;
            }
            try {
                throw toThrow;
            } catch (Exception ex) {
                // usual template
                LOGGER.error("Unexpected exception:" + toThrow.getMessage(), toThrow);
                throw toThrow;
            }
        }
    }

    @ExtendWith(LoggingExtension.class)
    private static class FakeClassForTest {
        @HideByExceptionClass({IllegalArgumentException.class, UnsupportedOperationException.class})
        private ClassWhichMayThrowException instance1 = new ClassWhichMayThrowException();
        @HideByExceptionMessage({"FYI message"})
        private ClassWhichMayThrowException instance2 = new ClassWhichMayThrowException();
    }

    @Test
    public void testFieldIsWrappedWithNewLogger() throws Exception {
        FakeClassForTest testInstance = new FakeClassForTest();

        loggingExtension.postProcessTestInstance(testInstance, null);

        assertNotNull(testInstance.instance1.LOGGER);
    }

    @Test
    public void testLoggerIsCalled() throws Exception {
        FakeClassForTest testInstance = new FakeClassForTest();

        loggingExtension.postProcessTestInstance(testInstance, null);
        try {
            testInstance.instance2.doOperate(new NullPointerException("FYI message"));
            fail();
        } catch (Exception ex) {
            assertFalse(sanitized.isEmpty());
            ArrayList<Object> values = new ArrayList<>(sanitized.values());
            assertEquals("java.lang.NullPointerException is hidden by message:FYI message", values.get(0));
            assertEquals("java.lang.NullPointerException is hidden by message:FYI message", values.get(1));
        }
    }
}