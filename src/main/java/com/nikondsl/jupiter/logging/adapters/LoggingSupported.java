package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;

public interface LoggingSupported {

    public boolean isClassAcceptableForReplacing(String className);

    public void setExceptionClassesToHide(Class[] values);
    public void setExceptionMessagesToHide(String[] values);
    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values);
    public Object sanitize(Object arg);
    public Object[] getSanitizedCopy(Object[] arguments);
}
