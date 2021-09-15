package com.nikondsl.utils;


import org.slf4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class LoggingExtension implements TestInstancePostProcessor {

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
            // do only for annotated fields in test class
            field.setAccessible(true);
            Object toInjectNewLogger = field.get(testInstance);
            // look for 'org.slf4j.Logger' there
            if (toInjectNewLogger == null) {
                throw new IllegalStateException("@" + anno + " annotated field (" + field.getName() + ") is null");
            }
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
                }
            }
        }
    }

    private void setUpLogger(String className,
                             Field field,
                             Object toInjectNewLogger,
                             Logger logger,
                             Class[] classesToHide,
                             String[] messagesToHide) throws ReflectiveOperationException {
        System.out.println("Setting up logger into '" + className +
                "." + field.getName() + "' with " + messagesToHide != null
                ? Arrays.asList(messagesToHide)
                : Arrays.asList(classesToHide));
        LoggerAdapter loggerAdapter = new LoggerAdapter(logger);
        loggerAdapter.setExceptionClassesToHide(classesToHide);
        loggerAdapter.setExceptionMessagesToHide(messagesToHide);
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            trySetStaticField(className, field, toInjectNewLogger, loggerAdapter);
        } else {
            //set dynamic field
            field.set(toInjectNewLogger, loggerAdapter);
        }
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
        field.set(toInjectNewLogger, newValue);
    }
}
