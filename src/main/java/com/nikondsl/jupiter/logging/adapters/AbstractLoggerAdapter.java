package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.adapters.impl.Slf4JLoggerAdapter;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractLoggerAdapter {
    private static List<AbstractLoggerAdapter> registeredAdapters = new ArrayList<>();

    static {
        registeredAdapters.add(new Slf4JLoggerAdapter(null));
    }

    public static AbstractLoggerAdapter createAdapter(Object logger) {
        if (logger instanceof Logger) {
            return new Slf4JLoggerAdapter((Logger) logger);
        }
        return null;
    }

    protected Set<Class> exceptionsToHide = Collections.emptySet();
    protected Set<String> messagesToHide = Collections.emptySet();
    protected List<ClassAndMessage> classAndMessageToHide = Collections.emptyList();

    public abstract boolean isFieldAcceptableForReplacing(String className);

    public static boolean isLoggerSupported(String className) {
        Optional found = registeredAdapters
                .stream()
                .filter(adapter -> adapter.isFieldAcceptableForReplacing(className))
                .findAny();
        return found.isPresent();
    }

    public void setExceptionClassesToHide(Class[] values) {
        if (values != null) {
            this.exceptionsToHide = new HashSet<>(Arrays.asList(values));
        }
    }

    public void setExceptionMessagesToHide(String[] values) {
        if (values != null) {
            this.messagesToHide = new HashSet<>(Arrays.asList(values));
        }
    }

    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values) {
        if (values != null) {
            this.classAndMessageToHide = Arrays.asList(values);
        }
    }
}
