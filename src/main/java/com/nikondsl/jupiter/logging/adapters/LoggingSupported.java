package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public interface LoggingSupported {

    boolean isClassAcceptableForReplacing(String className);
    void setExceptionClassesToHide(Class[] values);
    void setExceptionMessagesToHide(String[] values);
    void setExceptionClassAndMessageToHide(ClassAndMessage[] values);
    void setSuspendLogging(AtomicBoolean suspendLogging);
    Object sanitize(Object arg);
    Object[] getSanitizedCopy(Object[] arguments);

    void unwrap(Field field, Object key) throws ReflectiveOperationException;
}
