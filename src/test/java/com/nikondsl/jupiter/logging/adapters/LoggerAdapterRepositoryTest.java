package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class LoggerAdapterRepositoryTest {
    private SimpleLoggerAdapter log1 = new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance());
    private SimpleLoggerAdapter log2 = new NoValidConstructorAdapter(LoggerAdapterRepository.getInstance());

    private ClassAndMessage anno = new ClassAndMessage() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return ClassAndMessage.class;
        }

        @Override
        public Class<? extends Throwable> clazz() {
            return IllegalArgumentException.class;
        }

        @Override
        public String message() {
            return "aaabbbccc";
        }
    };

    static class SimpleLoggerAdapter implements LoggingSupported {
        private Object logger = null;
        private LoggerAdapterRepository delegate;
        private Class[] classesValues = null;
        private String[] stringValues = null;
        private ClassAndMessage[] values = null;
        private AtomicBoolean suspendLogging = new AtomicBoolean();

        public SimpleLoggerAdapter(Object logger, LoggerAdapterRepository delegate) {
            this.logger = logger;
            this.delegate = delegate;
        }

        @Override
        public boolean isClassAcceptableForReplacing(String className) {
            return getClass().getCanonicalName().equalsIgnoreCase(className);
        }

        @Override
        public void setExceptionClassesToHide(Class[] values) {
            this.classesValues = values;
        }

        @Override
        public void setExceptionMessagesToHide(String[] values) {
            this.stringValues = values;
        }

        @Override
        public void setExceptionClassAndMessageToHide(ClassAndMessage[] values) {
            this.values = values;
        }

        @Override
        public void setSuspendLogging(AtomicBoolean suspendLogging) {
            this.suspendLogging = suspendLogging;
        }

        @Override
        public Object sanitize(Object arg) {
            if (arg instanceof Exception) {
                return arg;
            }
            return "sanitized[" + arg + "]";
        }

        @Override
        public Object[] getSanitizedCopy(Object[] arguments) {
            return Arrays.copyOf(arguments, arguments.length);
        }

        @Override
        public void unwrap(Field field, Object key) throws ReflectiveOperationException {
            field.set(key, logger);
        }
    }
    static class NoValidConstructorAdapter extends SimpleLoggerAdapter implements LoggingSupported {

        public NoValidConstructorAdapter(LoggerAdapterRepository delegate) {
            super(null, delegate);
        }
    }

    @Spy
    private LoggerAdapterRepository repository;

    @AfterEach
    public void tearDown() {
        LoggerAdapterRepository.getInstance().setSuspendLogic(new AtomicBoolean(false));
        LoggerAdapterRepository.getInstance().setExceptionClassesToHide(null);
        LoggerAdapterRepository.getInstance().setExceptionMessagesToHide(null);
        LoggerAdapterRepository.getInstance().setExceptionClassAndMessageToHide(null);
    }

    @Test
    public void testCreate() {
        LoggerAdapterRepository.registeredAdaptors.add(new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance()));

        Object adapter = LoggerAdapterRepository.createAdapter(log1);

        assertNotNull(adapter);
        assertEquals(log1.getClass().getCanonicalName(), adapter.getClass().getCanonicalName());
    }

    @Test
    public void testCreateBad() {
        LoggerAdapterRepository.registeredAdaptors.add(new NoValidConstructorAdapter(LoggerAdapterRepository.getInstance()));

        assertThrows(IllegalArgumentException.class, () -> LoggerAdapterRepository.createAdapter(log2));
    }

    @Test
    public void isLoggerSupported() {
        LoggerAdapterRepository.registeredAdaptors.add(new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance()));

        assertTrue(LoggerAdapterRepository.isLoggerSupported(log1.getClass().getCanonicalName()));
        assertFalse(LoggerAdapterRepository.isLoggerSupported(log2.getClass().getCanonicalName()));
    }

    @Test
    public void sanitizeNull() {
        assertNull(LoggerAdapterRepository.getInstance().sanitize(null));
    }

    @Test
    public void sanitizeExceptionSuspended() {
        LoggerAdapterRepository.getInstance().setSuspendLogic(new AtomicBoolean(true));

        String result = (String) LoggerAdapterRepository.getInstance().sanitize(new IllegalArgumentException());

        assertEquals("java.lang.IllegalArgumentException is suspended", result);
    }

    @Test
    public void sanitizeExceptionOk() {
        IllegalArgumentException exception = new IllegalArgumentException();

        Exception result = (Exception) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertSame(exception, result);
    }

    @Test
    public void sanitizeExceptionMatchException() {
        LoggerAdapterRepository.getInstance().setExceptionClassesToHide(new Class[] {IllegalArgumentException.class});
        IllegalArgumentException exception = new IllegalArgumentException("abc");

        String result = (String) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertEquals("java.lang.IllegalArgumentException is hidden by class", result);
    }

    @Test
    public void sanitizeExceptionDoesNotMatchException() {
        LoggerAdapterRepository.getInstance().setExceptionClassesToHide(new Class[] {IllegalStateException.class});
        IllegalArgumentException exception = new IllegalArgumentException("abc");

        Exception result = (Exception) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertSame(exception, result);
    }

    @Test
    public void sanitizeExceptionMatchExceptionAndMessage() {
        LoggerAdapterRepository.getInstance().setExceptionClassAndMessageToHide(new ClassAndMessage[] {
            anno
        });
        IllegalArgumentException exception = new IllegalArgumentException("aaabbbccc");

        String result = (String) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertEquals("java.lang.IllegalArgumentException is hidden by class: java.lang.IllegalArgumentException and message:aaabbbccc", result);
    }

    @Test
    public void sanitizeExceptionDoesNotMatchExceptionAndMessage1() {
        LoggerAdapterRepository.getInstance().setExceptionClassAndMessageToHide(new ClassAndMessage[] {
                anno
        });
        IllegalArgumentException exception = new IllegalArgumentException("abc");

        Exception result = (Exception) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertSame(exception, result);
    }

    @Test
    public void sanitizeExceptionDoesNotMatchExceptionAndMessage2() {
        LoggerAdapterRepository.getInstance().setExceptionClassAndMessageToHide(new ClassAndMessage[] {
                anno
        });
        IllegalStateException exception = new IllegalStateException("aaabbbccc");

        Exception result = (Exception) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertSame(exception, result);
    }

    @Test
    public void sanitizeExceptionMatchMessage() {
        LoggerAdapterRepository.getInstance().setExceptionMessagesToHide(new String[] {"aaabbbccc"});
        IllegalArgumentException exception = new IllegalArgumentException("aaabbbccc");

        String result = (String) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertEquals("java.lang.IllegalArgumentException is hidden by message:aaabbbccc", result);
    }

    @Test
    public void sanitizeExceptionDoesNotMatchMessage() {
        LoggerAdapterRepository.getInstance().setExceptionMessagesToHide(new String[] {"aaabbbccc"});
        IllegalArgumentException exception = new IllegalArgumentException("abc");

        Exception result = (Exception) LoggerAdapterRepository.getInstance().sanitize(exception);

        assertSame(exception, result);
    }

    @Test
    public void getSanitizedCopyExceptionMatchMessage() {
        LoggerAdapterRepository.getInstance().setExceptionMessagesToHide(new String[] {"aaabbbccc"});
        IllegalArgumentException exception = new IllegalArgumentException("aaabbbccc");

        Object[] result = LoggerAdapterRepository.getInstance().getSanitizedCopy(new Object[] {
                "start", exception, "end"
        });

        assertEquals(3, result.length);
        assertEquals("start", result[0]);
        assertEquals("java.lang.IllegalArgumentException is hidden by message:aaabbbccc", result[1]);
        assertEquals("end", result[2]);
    }

    @Test
    public void getSanitizedCopyNull() {
        Object[] result = LoggerAdapterRepository.getInstance().getSanitizedCopy(null);

        assertNull(result);
    }
}