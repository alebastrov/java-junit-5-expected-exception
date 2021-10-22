package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
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

    static class NoValidConstructorAdapter extends SimpleLoggerAdapter implements LoggingSupported {
        public NoValidConstructorAdapter(LoggerAdapterRepository delegate) {
            super(null, delegate);
        }
    }

    @Spy
    private LoggerAdapterRepository repository;

    @BeforeEach
    public void setUp() {
        clear();
    }

    private static void clear() {
        LoggerAdapterRepository.getInstance().setSuspendLogic(new AtomicBoolean(false));
        LoggerAdapterRepository.getInstance().setExceptionClassesToHide(null);
        LoggerAdapterRepository.getInstance().setExceptionMessagesToHide(null);
        LoggerAdapterRepository.getInstance().setExceptionClassAndMessageToHide(null);
    }

    @AfterAll
    public static void tearDown() {
        clear();
    }

    @Test
    public void testCreate() {
        LoggerAdapterRepository.addToRegisteredAdaptors(new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance()));

        Object adapter = LoggerAdapterRepository.createAdaptor(log1);

        assertNotNull(adapter);
        assertEquals(log1.getClass().getCanonicalName(), adapter.getClass().getCanonicalName());
    }

    @Test
    public void testCreateBad() {
        LoggerAdapterRepository.addToRegisteredAdaptors(new NoValidConstructorAdapter(LoggerAdapterRepository.getInstance()));

        assertThrows(IllegalArgumentException.class, () -> LoggerAdapterRepository.createAdaptor(log2));
    }

    @Test
    public void isLoggerSupported() {
        LoggerAdapterRepository.addToRegisteredAdaptors(new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance()));

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
    public void sanitizeExceptionMatchesException() {
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
    public void sanitizeExceptionMatchesExceptionAndMessage() {
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
    public void sanitizeExceptionMatchesMessage() {
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
    public void getSanitizedCopyExceptionMatchesMessage() {
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