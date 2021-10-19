package com.nikondsl.jupiter.logging.adapters.impl;

import com.nikondsl.jupiter.logging.adapters.LoggerAdapterRepository;
import com.nikondsl.jupiter.logging.adapters.LoggingSupported;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Log4jLoggerAdapter extends Logger implements LoggingSupported {
    private Logger logger;
    private LoggerAdapterRepository delegate;

    public Log4jLoggerAdapter(Object logger, LoggerAdapterRepository delegate) {
        super("Log4jLoggerAdapter");
        this.logger = (Logger) logger;
        this.delegate = delegate;
    }

    @Override
    public boolean isClassAcceptableForReplacing(String className) {
        return "org.apache.log4j.Logger".equals(className);
    }

    @Override
    public void setExceptionClassesToHide(Class[] values) {
        delegate.setExceptionClassesToHide(values);
    }

    @Override
    public void setExceptionMessagesToHide(String[] values) {
        delegate.setExceptionMessagesToHide(values);
    }

    @Override
    public void setExceptionClassAndMessageToHide(ClassAndMessage[] values) {
        delegate.setExceptionClassAndMessageToHide(values);
    }

    @Override
    public void setSuspendLogging(AtomicBoolean suspendLogging) {
        if (delegate != null) {
            delegate.setSuspendLogic(suspendLogging);
        }
    }

    @Override
    public Object sanitize(Object arg) {
        return delegate.sanitize(arg);
    }

    @Override
    public Object[] getSanitizedCopy(Object[] arguments) {
        return delegate.getSanitizedCopy(arguments);
    }

    @Override
    public void unwrap(Field field, Object key) throws ReflectiveOperationException {
        field.set(key, logger);
    }

    public void trace(Object message) {
        logger.trace(message);
    }

    public void trace(Object message, Throwable t) {
        Object sanitized = sanitize(t);
        if (sanitized instanceof Throwable) {
            logger.trace(message, (Throwable) sanitized);
        } else {
            logger.trace(message + " " + sanitized);
        }
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void addAppender(Appender newAppender) {
        logger.addAppender(newAppender);
    }

    public void assertLog(boolean assertion, String msg) {
        logger.assertLog(assertion, msg);
    }

    public void callAppenders(LoggingEvent event) {
        logger.callAppenders(event);
    }

    public void debug(Object message) {
        logger.debug(message);
    }

    public void debug(Object message, Throwable t) {
        Object sanitized = sanitize(t);
        if (sanitized instanceof Throwable) {
            logger.debug(message, (Throwable) sanitized);
        } else {
            logger.debug(message + " " + sanitized);
        }
    }

    public void error(Object message) {
        logger.error(message);
    }

    public void error(Object message, Throwable t) {
        Object sanitized = sanitize(t);
        if (sanitized instanceof Throwable) {
            logger.error(message, (Throwable) sanitized);
        } else {
            logger.error(message + " " + sanitized);
        }
    }

    public void fatal(Object message) {
        logger.fatal(message);
    }

    public void fatal(Object message, Throwable t) {
        Object sanitized = sanitize(t);
        if (sanitized instanceof Throwable) {
            logger.fatal(message, (Throwable) sanitized);
        } else {
            logger.fatal(message + " " + sanitized);
        }
    }

    public boolean getAdditivity() {
        return logger.getAdditivity();
    }

    public Enumeration getAllAppenders() {
        return logger.getAllAppenders();
    }

    public Appender getAppender(String name) {
        return logger.getAppender(name);
    }

    public Level getEffectiveLevel() {
        return logger.getEffectiveLevel();
    }

    public Priority getChainedPriority() {
        return logger.getChainedPriority();
    }

    public LoggerRepository getHierarchy() {
        return logger.getHierarchy();
    }

    public LoggerRepository getLoggerRepository() {
        return logger.getLoggerRepository();
    }

    public ResourceBundle getResourceBundle() {
        return logger.getResourceBundle();
    }

    public void info(Object message) {
        logger.info(message);
    }

    public void info(Object message, Throwable t) {
        logger.info(message, (Throwable) sanitize(t));
    }

    public boolean isAttached(Appender appender) {
        return logger.isAttached(appender);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isEnabledFor(Priority level) {
        return logger.isEnabledFor(level);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void l7dlog(Priority priority, String key, Throwable t) {
        logger.l7dlog(priority, key, (Throwable) sanitize(t));
    }

    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
        logger.l7dlog(priority, key, params, (Throwable) sanitize(t));
    }

    public void log(Priority priority, Object message, Throwable t) {
        logger.log(priority, message, (Throwable) sanitize(t));
    }

    public void log(Priority priority, Object message) {
        logger.log(priority, message);
    }

    public void log(String callerFQCN, Priority level, Object message, Throwable t) {
        logger.log(callerFQCN, level, message, (Throwable) sanitize(t));
    }

    public void removeAllAppenders() {
        logger.removeAllAppenders();
    }

    public void removeAppender(Appender appender) {
        logger.removeAppender(appender);
    }

    public void removeAppender(String name) {
        logger.removeAppender(name);
    }

    public void setAdditivity(boolean additive) {
        logger.setAdditivity(additive);
    }

    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    public void setPriority(Priority priority) {
        logger.setPriority(priority);
    }

    public void setResourceBundle(ResourceBundle bundle) {
        logger.setResourceBundle(bundle);
    }

    public void warn(Object message) {
        logger.warn(message);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(message, (Throwable) sanitize(t));
    }
}
