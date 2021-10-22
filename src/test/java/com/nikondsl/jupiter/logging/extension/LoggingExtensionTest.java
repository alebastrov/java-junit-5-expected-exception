package com.nikondsl.jupiter.logging.extension;

import com.nikondsl.jupiter.logging.adapters.LoggerAdapterRepository;
import com.nikondsl.jupiter.logging.adapters.SimpleLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class LoggingExtensionTest {
    @Spy
    private LoggingExtension extension;
    private InnerWithLogger innerWithLogger = new InnerWithLogger();

    static enum YesNo {
        YES, NO;
    }

    private static class InnerWithLogger {
        int intPrimitive = 0;
        char[] charsPrimitive;
        YesNo enumPrimitive = YesNo.YES;
        LoggingExtensionTest classPrimitive;
        SimpleLoggerAdapter logger =
                new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance());
    }


    @Test
    public void lookForAndReplaceLogger() throws Exception {
        LoggerAdapterRepository.addToRegisteredAdaptors(new SimpleLoggerAdapter(null, LoggerAdapterRepository.getInstance()));
        doReturn(true).when(extension).wrapLogger(
                any(),
                eq(innerWithLogger),
                any(),
                eq(innerWithLogger.logger));

        assertTrue(extension.lookForAndReplaceLogger(null, innerWithLogger));
    }
}