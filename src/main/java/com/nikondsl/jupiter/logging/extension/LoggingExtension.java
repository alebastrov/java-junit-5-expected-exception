package com.nikondsl.jupiter.logging.extension;


import com.nikondsl.jupiter.logging.adapters.LoggerAdapterRepository;
import com.nikondsl.jupiter.logging.adapters.LoggingSupported;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.ClassesToWrapLoggers;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClass;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionClassAndMessage;
import com.nikondsl.jupiter.logging.annotations.HideByExceptionMessage;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
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

public class LoggingExtension implements TestInstancePostProcessor, AfterAllCallback {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingExtension.class);
    private static final ConcurrentMap<Object, List<Field>> toRevert = new ConcurrentHashMap<>();
    private static AtomicBoolean suspendLogging = new AtomicBoolean();
    private static AtomicBoolean initialized = new AtomicBoolean();

    static class ExtensionParams {
        private final Class<? extends Throwable>[] hideByClass;
        private final String[] hideByMessage;
        private final ClassAndMessage[] hideByBoth;

        public ExtensionParams(Class<? extends Throwable>[] hideByClass,
                               String[] hideByMessage,
                               ClassAndMessage[] hideByBoth) {

            this.hideByClass = hideByClass;
            this.hideByMessage = hideByMessage;
            this.hideByBoth = hideByBoth;
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        if (initialized.get()) {
            return;
        }
        initialized.set(true);
        Class clazz = testInstance.getClass();
        if (!clazz.getCanonicalName().contains("ReplaceOutsideDemoTest")) {
            return;
        }
        toRevert.clear();
        for (Field field : clazz.getDeclaredFields()) {
            if (!suspendLogging.get() &&
                !field.isAnnotationPresent(HideByExceptionClass.class) &&
                !field.isAnnotationPresent(HideByExceptionMessage.class) &&
                !field.isAnnotationPresent(HideByExceptionClassAndMessage.class)) {
                continue;
            }

            Class<? extends Throwable>[] classesToHide = getHideByExceptionClassValue(field, null);
            String[] messagesToHide = getHideByMessageValue(field, null);
            ClassAndMessage[] classAndMessageToHide = getHideByMessageAndClassValue(field, null);
            ExtensionParams params = new ExtensionParams(classesToHide, messagesToHide, classAndMessageToHide);

            String anno = getAnnoUsed(null, null, null, params);
            LOG.debug("Field with annotation @" + anno + " is found in class: " + clazz.getCanonicalName());

            // do only for annotated fields in test class
            field.setAccessible(true);
            Object toInjectNewLogger = field.get(testInstance);
            // look for 'org.slf4j.Logger' there
            if (toInjectNewLogger == null) {
                LOG.error("Field with annotation @" + anno + " is not initialized now");
                throw new IllegalStateException("@" + anno + " annotated field (" + field.getName() + ") is null");
            }

            if (!lookForAndReplaceLogger(params, toInjectNewLogger)) {
                LOG.warn("Logger field is not found in class: " + toInjectNewLogger.getClass().getCanonicalName());
            }
        }
        if (clazz.isAnnotationPresent(ClassesToWrapLoggers.class)) {
            ClassesToWrapLoggers toReplaceLoggers = (ClassesToWrapLoggers) clazz.getAnnotation(ClassesToWrapLoggers.class);
            for (Class toReplaceLogger : toReplaceLoggers.value()) {
                ExtensionParams params = new ExtensionParams(getHideByExceptionClassValue(null, clazz),
                        getHideByMessageValue(null, clazz),
                        getHideByMessageAndClassValue(null, clazz));
                if (!lookForAndReplaceLogger(params, toReplaceLogger)) {
                    LOG.warn("Logger field is not found in class: " + toReplaceLogger.getClass().getCanonicalName());
                }
            }
        }
    }

    private String getAnnoUsed(Class[] classesToHide1,
                               String[] messagesToHide1,
                               ClassAndMessage[] classAndMessageToHide1,
                               ExtensionParams params) {
        String anno = "HideByExceptionMessage";
        if (params.hideByClass != null) {
            anno = "HideByExceptionClass";
        }
        if (params.hideByMessage != null) {
            anno = "HideByExceptionMessage";
        }
        if (params.hideByBoth != null) {
            anno = "HideByExceptionClassAndMessage";
        }
        return anno;
    }

    private Class<? extends Throwable>[] getHideByExceptionClassValue(Field field, Class clazz) {
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

    boolean lookForAndReplaceLogger(ExtensionParams params,
                                    Object toInjectNewLogger) throws ReflectiveOperationException {
        boolean newLoggerSet = false;
        LOG.trace("Checking class: " + toInjectNewLogger + " | "+ toInjectNewLogger.hashCode());
        Field[] fields = null;
        if (toInjectNewLogger instanceof Class) {
            fields = ((Class) toInjectNewLogger).getDeclaredFields();
        } else {
            fields = toInjectNewLogger.getClass().getDeclaredFields();
        }
        boolean revertAccessibleFlag = false;
        for (Field lookForLogger : fields) {
            LOG.trace("Checking field: " + lookForLogger.getType().getCanonicalName());
            if (!lookForLogger.isAccessible()) {
                lookForLogger.setAccessible(true);
                revertAccessibleFlag = true;
            }
            if (lookForLogger.getType().isPrimitive() ||
                lookForLogger.getType().isArray() ||
                lookForLogger.getType().isEnum()) {
                if (revertAccessibleFlag) {
                    lookForLogger.setAccessible(false);
                }
                LOG.trace("Checked field: " + lookForLogger.getName() + ". primitive");
                continue;
            }
            if (!LoggerAdapterRepository.isLoggerSupported(lookForLogger.getType().getCanonicalName())) {
                LOG.trace("Checked field: " + lookForLogger.getName() + ". is not supported");
                if (revertAccessibleFlag) {
                    lookForLogger.setAccessible(false);
                }
                continue;
            }
            LOG.trace("Found supported field: " + lookForLogger.getName() +" => "+ lookForLogger.getType());
            Object possibleLogger = lookForLogger.get(toInjectNewLogger);
            LOG.trace("Wrapping logger " + possibleLogger.toString());
            newLoggerSet |= wrapLogger(params, toInjectNewLogger, lookForLogger, possibleLogger);
            if (revertAccessibleFlag) {
                lookForLogger.setAccessible(false);
            }
        }
        return newLoggerSet;
    }

    boolean wrapLogger(ExtensionParams params,
                       Object containerForLogger,
                       Field loggerField,
                       Object loggerObject) throws ReflectiveOperationException {
        LoggingSupported loggerAdaptor = createLoggerAdaptor(loggerObject);
        if (loggerAdaptor == null) {
            return false;
        }
        setUpLogger(containerForLogger.getClass().getCanonicalName(),
                loggerField,
                containerForLogger,
                loggerAdaptor,
                params);
        return true;
    }

    private void setUpLogger(String className,
                             Field field,
                             Object toInjectNewLogger,
                             LoggingSupported loggerAdapter,
                             ExtensionParams params) throws ReflectiveOperationException {
        LOG.trace("Setting up logger into '" + className +
                "." + field.getName() + "' with " + parameters(null, null, null, params));
        loggerAdapter.setSuspendLogging(suspendLogging);
        loggerAdapter.setExceptionClassesToHide(params.hideByClass);
        loggerAdapter.setExceptionMessagesToHide(params.hideByMessage);
        loggerAdapter.setExceptionClassAndMessageToHide(params.hideByBoth);
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

    private String parameters(Class[] classesToHide1,
                              String[] messagesToHide1,
                              ClassAndMessage[] classAndMessageToHide1,
                              ExtensionParams params) {
        StringBuilder result = new StringBuilder();

        if (params.hideByMessage != null) {
            result.append("message contains any of ").append(Arrays.asList(params.hideByMessage));
        }
        if (params.hideByClass != null) {
            result.append("class is one of ").append(Arrays.asList(params.hideByClass));
        }
        if (params.hideByBoth != null) {
            result.append("exception is one of ");
            Arrays
                    .stream(params.hideByBoth)
                    .forEach(classAndMessage -> {
                        result.append("class: ").append(classAndMessage.clazz()).append(" with message containing: ").append(classAndMessage.message());
                    });
        }
        return result.toString();
    }

    protected LoggingSupported createLoggerAdaptor(Object logger) {
        return LoggerAdapterRepository.createAdaptor(logger);
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
                throw new RuntimeException("Could not set wrapper for field. Please remove 'final' modifier from field '" +
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
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        for(Map.Entry<Object, List<Field>> entry : toRevert.entrySet()) {
            boolean revertFlag = false;
            for (Field field : entry.getValue()) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    revertFlag = true;
                }
                if (field.get(entry.getKey()) instanceof LoggingSupported) {
                    LoggingSupported logger = (LoggingSupported) field.get(entry.getKey());
                    logger.unwrap(field, entry.getKey());
                    LOG.debug("Old Logger is reverted for field '" + field.getName() +
                            "' in class: " +
                            entry.getKey().getClass().getCanonicalName());
                }
                if (revertFlag) {
                    field.setAccessible(false);
                }
            }
        }
        toRevert.clear();
    }

    public static void setSuspendLogging(boolean suspendLogging) {
        LoggingExtension.suspendLogging = new AtomicBoolean(suspendLogging);
    }
}
