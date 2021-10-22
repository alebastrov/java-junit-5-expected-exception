package com.nikondsl.jupiter.logging.adapters.impl;

import com.nikondsl.jupiter.logging.adapters.LoggerAdapterRepository;
import com.nikondsl.jupiter.logging.adapters.LoggingSupported;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class Log4j2LoggerAdaptor extends Logger implements LoggingSupported {
    private Logger logger;
    private LoggerAdapterRepository delegate;

    public Log4j2LoggerAdaptor(LoggerAdapterRepository delegate) {
        super(new LoggerContext("Log4j2LoggerContext"), "Log4j2LoggerAdapter", null);
        this.delegate = delegate;
    }

    public Log4j2LoggerAdaptor(Object logger, LoggerAdapterRepository delegate) {
        super(((Logger) logger).getContext(),
        "Log4j2LoggerAdapter",
              ((Logger) logger).getMessageFactory());
        this.logger = (Logger) logger;
        this.delegate = delegate;
    }

    @Override
    public boolean isClassAcceptableForReplacing(String className) {
        return "org.apache.logging.log4j.Logger".equals(className) ||
               "org.apache.logging.log4j.core.Logger".equals(className);
    }

    @Override
    public void setExceptionClassesToHide(Class[] values) {
        delegate.setExceptionClassesToHide(values);
    }

    @Override
    public void setExceptionMessagesToHide(String[] values) {
        delegate.setExceptionMessagesToHide(values);
    }

    @Override
    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values) {
        delegate.setExceptionClassAndMessageToHide(values);
    }

    @Override
    public void setSuspendLogging(AtomicBoolean suspendLogging) {
        if (delegate != null) {
            delegate.setSuspendLogic(suspendLogging);
        }
    }

    @Override
    public Object sanitize(Object arg) {
        return delegate.sanitize(arg);
    }

    @Override
    public Object[] getSanitizedCopy(Object[] arguments) {
        return delegate.getSanitizedCopy(arguments);
    }

    @Override
    public void unwrap(Field field, Object key) throws ReflectiveOperationException {
        field.set(key, logger);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Throwable throwable) {
        if (super.isEnabled(level, marker, message, throwable)) {
            Object sanitize = sanitize(throwable);
            if (sanitize instanceof Throwable) {
                super.logMessage(fqcn, level, marker, message, sanitize);
            } else {
                super.logMessage(fqcn, level, marker, message + sanitize);
            }
        }
    }
}
