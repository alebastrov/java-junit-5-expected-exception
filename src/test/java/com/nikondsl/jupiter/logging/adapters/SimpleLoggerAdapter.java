package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleLoggerAdapter implements LoggingSupported {
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
