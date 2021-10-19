package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.adapters.impl.Log4j2LoggerAdapter;
import com.nikondsl.jupiter.logging.adapters.impl.Log4jLoggerAdapter;
import com.nikondsl.jupiter.logging.adapters.impl.Slf4JLoggerAdapter;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractLoggerAdapter {
    private static List<LoggingSupported> registeredAdapters = new ArrayList<>();
    private static AbstractLoggerAdapter instance = new AbstractLoggerAdapter();
    private AtomicBoolean suspendLogic = new AtomicBoolean();

    static {
        registeredAdapters.add(new Slf4JLoggerAdapter(null, instance));
        registeredAdapters.add(new Log4jLoggerAdapter(null, instance));
        registeredAdapters.add(new Log4j2LoggerAdapter(instance));
    }

    public static LoggingSupported createAdapter(Object logger) {
        Optional found = registeredAdapters
            .stream()
            .filter(adapter -> adapter.isClassAcceptableForReplacing(logger.getClass().getCanonicalName()))
            .map(adapter -> {
                try {
                    Constructor constructor = adapter.getClass().getConstructor(Object.class, AbstractLoggerAdapter.class);
                    return (LoggingSupported) constructor.newInstance(logger, instance);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace(System.err);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .findAny();
        return found.isPresent() ? (LoggingSupported) found.get() : null;
    }

    protected Set<Class> exceptionsToHide = Collections.emptySet();
    protected Set<String> messagesToHide = Collections.emptySet();
    protected List<ClassAndMessage> classAndMessageToHide = Collections.emptyList();

    public static boolean isLoggerSupported(String className) {
        Optional found = registeredAdapters
                .stream()
                .filter(adapter -> adapter.isClassAcceptableForReplacing(className))
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

    public void setSuspendLogic(AtomicBoolean suspendLogic) {
        this.suspendLogic = suspendLogic;
    }

    public Object sanitize(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Exception) {
            if (suspendLogic.get()) {
                return (arg.getClass().getCanonicalName() + " is suspended");
            }
            if (exceptionsToHide.contains(arg.getClass())) {
                return (arg.getClass().getCanonicalName() + " is hidden by class");
            }
            if (!messagesToHide.isEmpty()) {
                Optional<String> patternFound = messagesToHide
                        .stream()
                        .filter(pattern ->
                                ((Exception) arg).getMessage().contains(pattern))
                        .findAny();
                if (patternFound.isPresent()) {
                    return (arg.getClass().getCanonicalName() + " is hidden by message:" + ((Exception) arg).getMessage());
                }
            }
            if (!classAndMessageToHide.isEmpty()) {
                Optional<ClassAndMessage> patternFound = classAndMessageToHide
                        .stream()
                        .filter(classAndMessage ->
                                classAndMessage.clazz().getClassLoader() == arg.getClass().getClassLoader() &&
                                        classAndMessage.clazz() == arg.getClass() &&
                                        ((Exception) arg).getMessage().contains(classAndMessage.message()))
                        .findAny();
                if (patternFound.isPresent()) {
                    return (arg.getClass().getCanonicalName() + " is hidden by class: " +
                            arg.getClass().getCanonicalName() + " and message:" +
                            ((Exception) arg).getMessage());
                }
            }
        }
        return arg;
    }

    public Object[] getSanitizedCopy(Object[] arguments) {
        List<Object> result = new ArrayList<>();
        for (Object obj : arguments) {
            if (obj instanceof Exception) {
                result.add(sanitize(obj));
            } else {
                result.add(obj);
            }
        }
        return result.toArray();
    }
}
