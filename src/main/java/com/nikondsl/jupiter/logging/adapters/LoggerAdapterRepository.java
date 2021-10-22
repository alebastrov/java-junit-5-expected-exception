package com.nikondsl.jupiter.logging.adapters;

import com.nikondsl.jupiter.logging.adapters.impl.Log4j2LoggerAdaptor;
import com.nikondsl.jupiter.logging.adapters.impl.Log4jLoggerAdaptor;
import com.nikondsl.jupiter.logging.adapters.impl.Slf4JLoggerAdaptor;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

public class LoggerAdapterRepository {
    private static Logger LOG = LoggerFactory.getLogger(LoggerAdapterRepository.class);
    private static List<LoggingSupported> registeredAdaptors = new ArrayList<>();
    private static LoggerAdapterRepository instance = new LoggerAdapterRepository();
    private AtomicBoolean suspendLogic = new AtomicBoolean();

    static {
       reInit();
    }

    public static LoggingSupported createAdaptor(Object logger) {
        final Exception[] exceptios = new Exception[1];
        Optional found = registeredAdaptors
            .stream()
//            .filter(adaptor -> adaptor.isClassAcceptableForReplacing(logger.getClass().getCanonicalName()))
            .map(adaptor -> {
                LOG.trace("Checking built-in adaptor " + adaptor.getClass().getCanonicalName());
                if (!adaptor.isClassAcceptableForReplacing(logger.getClass().getCanonicalName())) {
                    LOG.trace("It does not support for name " + logger.getClass().getCanonicalName());
                    return null;
                }
                LOG.trace("It supports for name " + logger.getClass().getCanonicalName());
                try {
                    Constructor constructor = adaptor.getClass().getConstructor(Object.class, LoggerAdapterRepository.class);
                    LoggingSupported loggingSupported = (LoggingSupported) constructor.newInstance(logger, instance);
                    LOG.trace("It returns OK for name " + logger.getClass().getCanonicalName());
                    return loggingSupported;
                } catch (Exception e) {
                    if (exceptios[0] == null) {
                        exceptios[0] = e;
                    }
                }
                LOG.trace("It returns BAD for name " + logger.getClass().getCanonicalName() + " -> " + exceptios[0]);
                return null;
            })
            .filter(Objects::nonNull)
            .findAny();
        if (!found.isPresent()) {
            String message = "Cannot find adaptor class for name '" + logger.getClass().getCanonicalName() +
                    "'\n\tThe target adaptor class should implement 'LoggingSupported' interface and \n\talso should have " +
                    "a public constructor with 2 arguments, \n\tObject.class and LoggerAdapterRepository.class. " +
                    "\nRegistered adaptors are:\n\t" +
                    registeredAdaptors.stream().map(ad -> ad.getClass().getCanonicalName()).collect(Collectors.joining(",\n\t"));
            if (exceptios[0] != null) {
                throw new IllegalArgumentException(message, exceptios[0]);
            }
            throw new IllegalArgumentException(message);
        }
        return (LoggingSupported) found.get();
    }

    protected Set<Class<? extends Throwable>> exceptionsToHide = Collections.emptySet();
    protected Set<String> messagesToHide = Collections.emptySet();
    protected List<ClassAndMessage> classAndMessageToHide = Collections.emptyList();

    public static boolean isLoggerSupported(String className) {
        Optional found = registeredAdaptors
                .stream()
                .filter(adapter -> adapter.isClassAcceptableForReplacing(className))
                .findAny();
        return found.isPresent();
    }

    public void setExceptionClassesToHide(Class<? extends Throwable>[] values) {
        if (values != null) {
            this.exceptionsToHide = new HashSet<>(Arrays.asList(values));
        } else {
            this.exceptionsToHide = Collections.emptySet();
        }
    }

    public void setExceptionMessagesToHide(String[] values) {
        if (values != null) {
            this.messagesToHide = new HashSet<>(Arrays.asList(values));
        } else {
            this.messagesToHide = Collections.emptySet();
        }
    }

    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values) {
        if (values != null) {
            this.classAndMessageToHide = Arrays.asList(values);
        } else {
            this.classAndMessageToHide = Collections.emptyList();
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
                            ((Exception) arg).getMessage() != null && ((Exception) arg).getMessage().contains(pattern))
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
        if (arguments == null) {
            return null;
        }
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

    public static LoggerAdapterRepository getInstance() {
        return instance;
    }

    public static void addToRegisteredAdaptors(LoggingSupported obj) {
        registeredAdaptors.add(obj);
    }

    public static void reInit() {
        registeredAdaptors.clear();
        registeredAdaptors.add(new Slf4JLoggerAdaptor(null, instance));
        registeredAdaptors.add(new Log4jLoggerAdaptor(null, instance));
        registeredAdaptors.add(new Log4j2LoggerAdaptor(instance));
    }

    public static List<LoggingSupported> getRegisteredAdaptors() {
        return Collections.unmodifiableList(registeredAdaptors);
    }
}
