package com.nikondsl.jupiter.logging.extension;


import com.nikondsl.jupiter.logging.adapters.AbstractLoggerAdapter;
import com.nikondsl.jupiter.logging.adapters.LoggingSupported;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.ClassesToWrapLoggers;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClass;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionMessage;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.slf4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingExtension implements TestInstancePostProcessor, TestInstancePreDestroyCallback {
    private static Logger LOG = LoggerFactory.getLogger(LoggingExtension.class);
    private static ConcurrentMap<Object, List<Field>> toRevert = new ConcurrentHashMap<>();
    private static AtomicBoolean suspendLogging = new AtomicBoolean();

    @Override
    public void postProcessTestInstance(Object testInstance,
                                        ExtensionContext context) throws Exception {
        //take a field to set up new logger
        Class clazz = testInstance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!suspendLogging.get() &&
                !field.isAnnotationPresent(HideByExceptionClass.class) &&
                !field.isAnnotationPresent(HideByExceptionMessage.class) &&
                !field.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
                continue;
            }

            Class[] classesToHide = getHideByExceptionClassValue(field, null);
            String[] messagesToHide = getHideByMessageValue(field, null);
            ClassAndMessage[] classAndMessageToHide = getHideByMessageAndClassValue(field, null);

            String anno = getAnnoUsed(classesToHide, messagesToHide, classAndMessageToHide);
            LOG.debug("Field with annotation @" + anno + " is found in class: " + clazz.getCanonicalName());

            // do only for annotated fields in test class
            field.setAccessible(true);
            Object toInjectNewLogger = field.get(testInstance);
            // look for 'org.slf4j.Logger' there
            if (toInjectNewLogger == null) {
                LOG.error("Field with annotation @" + anno + " is not initialized now");
                throw new IllegalStateException("@" + anno + " annotated field (" + field.getName() + ") is null");
            }
            if (!lookForAndReplaceLogger(classesToHide, messagesToHide, classAndMessageToHide, toInjectNewLogger)) {
                LOG.warn("Logger field is not found in class: " + toInjectNewLogger.getClass().getCanonicalName());
            }
        }
        if (clazz.isAnnotationPresent(ClassesToWrapLoggers.class)) {
            ClassesToWrapLoggers toReplaceLoggers = (ClassesToWrapLoggers) clazz.getAnnotation(ClassesToWrapLoggers.class);
            for (Class toReplaceLogger : toReplaceLoggers.value()) {
                if (!lookForAndReplaceLogger(
                        getHideByExceptionClassValue(null, clazz),
                        getHideByMessageValue(null, clazz),
                        getHideByMessageAndClassValue(null, clazz),
                        (Class) toReplaceLogger)) {
                    LOG.warn("Logger field is not found in class: " + toReplaceLogger.getClass().getCanonicalName());
                }
            }
        }
    }

    private String getAnnoUsed(Class[] classesToHide, String[] messagesToHide, ClassAndMessage[] classAndMessageToHide) {
        String anno = "HideByExceptionMessage";
        if (classesToHide != null) {
            anno = "HideByExceptionClass";
        }
        if (messagesToHide != null) {
            anno = "HideByExceptionMessage";
        }
        if (classAndMessageToHide != null) {
            anno = "HideByExceptionClassAndMessage";
        }
        return anno;
    }

    private Class[] getHideByExceptionClassValue(Field field, Class clazz) {
        if (field != null && field.isAnnotationPresent(HideByExceptionClass.class)) {
            return field.getAnnotation(HideByExceptionClass.class).value();
        }
        if (clazz != null && clazz.isAnnotationPresent(HideByExceptionClass.class)) {
            return ((HideByExceptionClass) clazz.getAnnotation(HideByExceptionClass.class)).value();
        }
        return null;
    }

    private String[] getHideByMessageValue(Field field, Class clazz) {
        if (field != null && field.isAnnotationPresent(HideByExceptionMessage.class)) {
            return field.getAnnotation(HideByExceptionMessage.class).value();
        }
        if (clazz != null && clazz.isAnnotationPresent(HideByExceptionMessage.class)) {
            return ((HideByExceptionMessage) clazz.getAnnotation(HideByExceptionMessage.class)).value();
        }
        return null;
    }

    private ClassAndMessage[] getHideByMessageAndClassValue(Field field, Class clazz) {
        if (field != null && field.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
            return field.getAnnotation(HideByExceptionClassAndMessage.class).value();
        }
        if (clazz != null && clazz.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
            return ((HideByExceptionClassAndMessage) clazz.getAnnotation(HideByExceptionClassAndMessage.class)).value();
        }
        return null;
    }

    private boolean lookForAndReplaceLogger(Class[] classesToHide,
                                            String[] messagesToHide,
                                            ClassAndMessage[] classAndMessageToHide,
                                            Object toInjectNewLogger) throws ReflectiveOperationException {
        boolean newLoggerSet = false;
        Field[] fields = null;
        if (toInjectNewLogger instanceof Class) {
            fields = ((Class) toInjectNewLogger).getDeclaredFields();
        } else {
            fields = toInjectNewLogger.getClass().getDeclaredFields();
        }
        for (Field lookForLogger : fields) {
            lookForLogger.setAccessible(true);
            if (lookForLogger.getType().isPrimitive() ||
                lookForLogger.getType().isArray() ||
                lookForLogger.getType().isEnum()) {
                continue;
            }
            if (!AbstractLoggerAdapter.isLoggerSupported(lookForLogger.getType().getCanonicalName())) {
                continue;
            }
            Object possibleLogger = lookForLogger.get(toInjectNewLogger);
            LoggingSupported loggerAdapter = createLoggerAdapter(possibleLogger);

            if (loggerAdapter != null) {
                setUpLogger(toInjectNewLogger.getClass().getCanonicalName(),
                        lookForLogger,
                        toInjectNewLogger,
                        loggerAdapter,
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
                             LoggingSupported loggerAdapter,
                             Class[] classesToHide,
                             String[] messagesToHide,
                             ClassAndMessage[] classAndMessageToHide) throws ReflectiveOperationException {
        LOG.debug("Setting up logger into '" + className +
                "." + field.getName() + "' with " + parameters(classesToHide, messagesToHide, classAndMessageToHide));
        loggerAdapter.setSuspendLogging(suspendLogging);
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
            addToRevert(toInjectNewLogger, field);
        }
    }

    private static void addToRevert(Object toRevertObject, Field toRevertField) {
        List<Field> newFields = Collections.synchronizedList(new ArrayList<>());
        List<Field> oldFields = toRevert.putIfAbsent(toRevertObject, newFields);
        if (oldFields == null) {
            oldFields = newFields;
        }
        oldFields.add(toRevertField);
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

    protected LoggingSupported createLoggerAdapter(Object logger) {
        return AbstractLoggerAdapter.createAdapter(logger);
    }

    static void trySetStaticField(String className,
                                  Field field,
                                  Object toInjectNewLogger,
                                  LoggingSupported newValue)
            throws ReflectiveOperationException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        if (java.lang.reflect.Modifier.isFinal(modifiersField.getModifiers())) {
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            try {
                field.set(null, newValue);
                addToRevert(toInjectNewLogger, field);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Please remove 'final' modifier from field '" +
                        field.getName() + "' in class '" + className + "'", ex);
            }
            return;
        }
        LOG.debug("New Logger is setting up for field '" + field.getName() + "' in class: " +
                toInjectNewLogger.getClass().getCanonicalName());
        field.set(toInjectNewLogger, newValue);
        addToRevert(toInjectNewLogger, field);
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext extensionContext) throws Exception {
        for(Map.Entry<Object, List<Field>> entry : toRevert.entrySet()) {
            for (Field field : entry.getValue()) {
                if (field.get(entry.getKey()) instanceof LoggingSupported) {
                    LoggingSupported logger = (LoggingSupported) field.get(entry.getKey());
                    logger.unwrap(field, entry.getKey());
                    LOG.debug("Old Logger is reverted for field '" + field.getName() +
                            "' in class: " +
                            entry.getKey().getClass().getCanonicalName());
                }
            }
        }
    }

    public static void setSuspendLogging(boolean suspendLogging) {
        LoggingExtension.suspendLogging = new AtomicBoolean(suspendLogging);
    }
}
