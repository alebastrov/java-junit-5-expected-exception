package com.nikondsl.logging.utils;


import org.slf4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class LoggingExtension implements TestInstancePostProcessor {
    private static Logger LOG = LoggerFactory.getLogger(LoggingExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance,
                                        ExtensionContext context) throws Exception {
        //take a field to set up new logger
        Class clazz = testInstance.getClass();
        Class[] toReplaceLoggers = new Class[0];
        if (clazz.isAnnotationPresent(ClassesToWrapLoggers.class)) {
            ClassesToWrapLoggers annotation = (ClassesToWrapLoggers) clazz.getAnnotation(ClassesToWrapLoggers.class);
            toReplaceLoggers = annotation.value();
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(HideByExceptionClass.class) &&
                !field.isAnnotationPresent(HideByExceptionMessage.class) &&
                !field.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
                continue;
            }

            String anno = "HideByExceptionMessage";
            Class[] classesToHide = null;
            String[] messagesToHide = null;
            ClassAndMessage[] classAndMessageToHide = null;
            if (field.isAnnotationPresent(HideByExceptionClass.class)) {
                anno = "HideByExceptionClass";
                classesToHide = field.getAnnotation(HideByExceptionClass.class).value();
            }
            if (field.isAnnotationPresent(HideByExceptionMessage.class)) {
                anno = "HideByExceptionClass";
                messagesToHide = field.getAnnotation(HideByExceptionMessage.class).value();
            }
            if (field.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
                anno = "HideByExceptionClassAndMessage";
                classAndMessageToHide = field.getAnnotation(HideByExceptionClassAndMessage.class).value();
            }
            LOG.debug("Field with annotation @" + anno + " is found in class: " + clazz.getCanonicalName());
            // do only for annotated fields in test class
            field.setAccessible(true);
            Object toInjectNewLogger = field.get(testInstance);
            // look for 'org.slf4j.Logger' there
            if (toInjectNewLogger == null) {
                LOG.error("Field with annotation @" + anno + " is not initialized now");
                throw new IllegalStateException("@" + anno + " annotated field (" + field.getName() + ") is null");
            }
            boolean newLoggerSet = lookForAndReplaceLogger(classesToHide, messagesToHide, classAndMessageToHide, toInjectNewLogger);
            for (Class toReplaceLogger : toReplaceLoggers) {
                newLoggerSet |= lookForAndReplaceLogger(classesToHide, messagesToHide, classAndMessageToHide, toReplaceLogger);
            }
            if (!newLoggerSet) {
                LOG.warn("Logger field is not found in class: " + toInjectNewLogger.getClass().getCanonicalName());
            }
        }
    }

    private boolean lookForAndReplaceLogger(Class[] classesToHide,
                                            String[] messagesToHide,
                                            ClassAndMessage[] classAndMessageToHide,
                                            Object toInjectNewLogger) throws ReflectiveOperationException {
        boolean newLoggerSet = false;
        for (Field lookForLogger : toInjectNewLogger.getClass().getDeclaredFields()) {
            lookForLogger.setAccessible(true);
            Object possibleLogger = lookForLogger.get(toInjectNewLogger);
            if (possibleLogger instanceof Logger) {
                setUpLogger(toInjectNewLogger.getClass().getCanonicalName(),
                        lookForLogger,
                        toInjectNewLogger,
                        (Logger) possibleLogger,
                        classesToHide,
                        messagesToHide,
                        classAndMessageToHide);
                newLoggerSet = true;
            }
        }
        return newLoggerSet;
    }

    private void setUpLogger(String className,
                             Field field,
                             Object toInjectNewLogger,
                             Logger logger,
                             Class[] classesToHide,
                             String[] messagesToHide,
                             ClassAndMessage[] classAndMessageToHide) throws ReflectiveOperationException {
        LOG.debug("Setting up logger into '" + className +
                "." + field.getName() + "' with " + parameters(classesToHide, messagesToHide, classAndMessageToHide));
        LoggerAdapter loggerAdapter = createLoggerAdaptor(logger);
        loggerAdapter.setExceptionClassesToHide(classesToHide);
        loggerAdapter.setExceptionMessagesToHide(messagesToHide);
        loggerAdapter.setExceptionClassAndMessageToHide(classAndMessageToHide);
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            LOG.debug("Field '" + field.getName() + "' static in class: " +
                    toInjectNewLogger.getClass().getCanonicalName());
            trySetStaticField(className, field, toInjectNewLogger, loggerAdapter);
        } else {
            //set dynamic field
            LOG.debug("Logger '" + field.getName() + "' in class: " +
                    toInjectNewLogger.getClass().getCanonicalName() + " is wrapped");
            field.set(toInjectNewLogger, loggerAdapter);
        }
    }

    private String parameters(Class[] classesToHide, String[] messagesToHide, ClassAndMessage[] classAndMessageToHide) {
        StringBuilder result = new StringBuilder();

        if (messagesToHide != null) {
            result.append("message contains any of ").append(Arrays.asList(messagesToHide));
        }
        if (classesToHide != null) {
            result.append("class is one of ").append(Arrays.asList(classesToHide));
        }
        if (classAndMessageToHide != null) {
            result.append("exception is one of ");
            Arrays
                    .stream(classAndMessageToHide)
                    .forEach(classAndMessage -> {
                        result.append("class: ").append(classAndMessage.clazz()).append(" with message containing: ").append(classAndMessage.message());
                    });
        }
        return result.toString();
    }

    protected LoggerAdapter createLoggerAdaptor(Logger logger) {
        return new LoggerAdapter(logger);
    }

    static void trySetStaticField(String className,
                                  Field field,
                                  Object toInjectNewLogger,
                                  LoggerAdapter newValue)
            throws ReflectiveOperationException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        if (java.lang.reflect.Modifier.isFinal(modifiersField.getModifiers())) {
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            try {
                field.set(null, newValue);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Please remove 'final' modifier from field '" +
                        field.getName() + "' in class '" + className + "'", ex);
            }
            return;
        }
        LOG.debug("New Logger is set up for field '" + field.getName() + "' in class: " +
                toInjectNewLogger.getClass().getCanonicalName());
        field.set(toInjectNewLogger, newValue);
    }
}
