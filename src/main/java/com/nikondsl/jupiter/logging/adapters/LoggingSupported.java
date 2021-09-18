package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public interface LoggingSupported {

    public boolean isClassAcceptableForReplacing(String className);

    public void setExceptionClassesToHide(Class[] values);
    public void setExceptionMessagesToHide(String[] values);
    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values);
    public Object sanitize(Object arg);
    public Object[] getSanitizedCopy(Object[] arguments);

    void unwrap(Field field, Object key) throws ReflectiveOperationException;
}
