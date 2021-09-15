package com.nikondsl.utils;


import org.slf4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class LoggingExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance,
                                        ExtensionContext context) throws Exception {
        //take a field to set up new logger
        Class clazz = testInstance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Mitigate.class)) {
                continue;
            }
            // do only for mitigated
            field.setAccessible(true);
            Object toInjectNewLogger = field.get(testInstance);
            // look for 'org.slf4j.Logger' there
            if (toInjectNewLogger == null) {
                throw new IllegalStateException("@Mitigate annotated field (" + field.getName() + ") is null");
            }
            for (Field lookForLogger : toInjectNewLogger.getClass().getDeclaredFields()) {
                lookForLogger.setAccessible(true);
                Object possibleLogger = lookForLogger.get(toInjectNewLogger);
                if (possibleLogger instanceof Logger) {
                    setUpMitigatedLogger(toInjectNewLogger.getClass().getCanonicalName(),
                            lookForLogger,
                            toInjectNewLogger,
                            (Logger) possibleLogger,
                            field.getAnnotation(Mitigate.class).value());
                }
            }
        }
    }

    private void setUpMitigatedLogger(String className,
                                      Field field,
                                      Object toInjectNewLogger,
                                      Logger logger,
                                      Class[] values) throws ReflectiveOperationException {
//        System.out.println("Setting up mitigated logger into '" + className +
//                "." + field.getName() + "' with " + Arrays.asList(values));
        LoggerAdapter loggerAdapter = new LoggerAdapter(logger);
        loggerAdapter.setMitigatedExceptions(values);
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
