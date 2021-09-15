package com.nikondsl.utils;


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
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(HideByExceptionClass.class) &&
                !field.isAnnotationPresent(HideByExceptionMessage.class)) {
                continue;
            }

            String anno = "HideByExceptionMessage";
            Class[] classesToHide = null;
            String[] messagesToHide = null;
            if (field.isAnnotationPresent(HideByExceptionClass.class)) {
                anno = "HideByExceptionClass";
                classesToHide = field.getAnnotation(HideByExceptionClass.class).value();
            }
            if (field.isAnnotationPresent(HideByExceptionMessage.class)) {
                anno = "HideByExceptionClass";
                messagesToHide = field.getAnnotation(HideByExceptionMessage.class).value();
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
                            messagesToHide);
                    newLoggerSet = true;
                }
            }
            if (!newLoggerSet) {
                LOG.warn("Logger field is not found in class: " + toInjectNewLogger.getClass().getCanonicalName());
            }
        }
    }

    private void setUpLogger(String className,
                             Field field,
                             Object toInjectNewLogger,
                             Logger logger,
                             Class[] classesToHide,
                             String[] messagesToHide) throws ReflectiveOperationException {
        LOG.debug("Setting up logger into '" + className +
                "." + field.getName() + "' with " + (messagesToHide != null
                ? Arrays.asList(messagesToHide)
                : Arrays.asList(classesToHide)));
        LoggerAdapter loggerAdapter = createLoggerAdaptor(logger);
        loggerAdapter.setExceptionClassesToHide(classesToHide);
        loggerAdapter.setExceptionMessagesToHide(messagesToHide);
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
