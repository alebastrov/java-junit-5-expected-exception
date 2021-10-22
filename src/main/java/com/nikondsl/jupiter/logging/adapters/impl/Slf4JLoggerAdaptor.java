package com.nikondsl.jupiter.logging.adapters.impl;

import com.nikondsl.jupiter.logging.adapters.LoggerAdapterRepository;
import com.nikondsl.jupiter.logging.adapters.LoggingSupported;
import com.nikondsl.jupiter.logging.annotations.ClassAndMessage;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class Slf4JLoggerAdaptor implements LoggingSupported, org.slf4j.Logger {

    private Logger logger;
    private LoggerAdapterRepository delegate;

    public Slf4JLoggerAdaptor(Object logger, LoggerAdapterRepository delegate) {
        this.logger = (Logger) logger;
        this.delegate = delegate;
    }

    @Override
    public boolean isClassAcceptableForReplacing(String className) {
        return "org.slf4j.Logger".equals(className) ||
               "org.apache.logging.slf4j.Log4jLogger".equals(className);
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

    /**
     * Return the name of this <code>Logger</code> instance.
     * @return name of this logger instance
     */
    @Override
    public String getName() {
        return logger.getName();
    }

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level,
     *         false otherwise.
     * @since 1.4
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     * @since 1.4
     */
    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @since 1.4
     */
    @Override
    public void trace(String format, Object arg) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, sanitize(arg));
        }
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @since 1.4
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, sanitize(arg1), sanitize(arg2));
        }
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for TRACE. The variants taking {@link #trace(String, Object) one} and
     * {@link #trace(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @since 1.4
     */
    @Override
    public void trace(String format, Object... arguments) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, getSanitizedCopy(arguments));
        }
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @since 1.4
     */
    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, sanitize(t));
    }

    /**
     * Similar to {@link #isTraceEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the TRACE level,
     *         false otherwise.
     *
     * @since 1.4
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the TRACE level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @since 1.4
     */
    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    /**
     * This method is similar to {@link #trace(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @since 1.4
     */
    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, format, sanitize(arg));
        }
    }

    /**
     * This method is similar to {@link #trace(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @since 1.4
     */
    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, format, sanitize(arg1), sanitize(arg2));
        }
    }

    /**
     * This method is similar to {@link #trace(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     * @since 1.4
     */
    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, format, getSanitizedCopy(argArray));
        }
    }

    /**
     * This method is similar to {@link #trace(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @since 1.4
     */
    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, sanitize(t));
    }

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *  @param format the format string
     * @param arg    the argument
     */
    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, sanitize(arg));
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *  @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *  @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, getSanitizedCopy(arguments));
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *  @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, sanitize(t));
    }

    /**
     * Similar to {@link #isDebugEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the DEBUG level.
     *  @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    /**
     * This method is similar to {@link #debug(String, Object)} method except that the
     * marker data is also taken into consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, sanitize(arg));
    }

    /**
     * This method is similar to {@link #debug(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * This method is similar to {@link #debug(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, getSanitizedCopy(arguments));
    }

    /**
     * This method is similar to {@link #debug(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *  @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, sanitize(t));
    }

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *  @param format the format string
     * @param arg    the argument
     */
    @Override
    public void info(String format, Object arg) {
        logger.info(format, sanitize(arg));
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *  @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *  @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, getSanitizedCopy(arguments));
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *  @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, sanitize(t));
    }

    /**
     * Similar to {@link #isInfoEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return true if this logger is warn enabled, false otherwise
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the INFO level.
     *  @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    /**
     * This method is similar to {@link #info(String, Object)} method except that the
     * marker data is also taken into consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, sanitize(arg));
    }

    /**
     * This method is similar to {@link #info(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * This method is similar to {@link #info(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, getSanitizedCopy(arguments));
    }

    /**
     * This method is similar to {@link #info(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *  @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, sanitize(t));
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *  @param format the format string
     * @param arg    the argument
     */
    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, sanitize(arg));
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *  @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, getSanitizedCopy(arguments));
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *  @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *  @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, sanitize(t));
    }

    /**
     * Similar to {@link #isWarnEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the WARN level.
     *  @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    /**
     * This method is similar to {@link #warn(String, Object)} method except that the
     * marker data is also taken into consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, sanitize(arg));
    }

    /**
     * This method is similar to {@link #warn(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * This method is similar to {@link #warn(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, getSanitizedCopy(arguments));
    }

    /**
     * This method is similar to {@link #warn(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *  @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, sanitize(t));
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *  @param format the format string
     * @param arg    the argument
     */
    @Override
    public void error(String format, Object arg) {
        logger.error(format, sanitize(arg));
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *  @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *  @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, getSanitizedCopy(arguments));
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *  @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, sanitize(t));
    }

    /**
     * Similar to {@link #isErrorEnabled()} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the ERROR level.
     *  @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    /**
     * This method is similar to {@link #error(String, Object)} method except that the
     * marker data is also taken into consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, sanitize(arg));
    }

    /**
     * This method is similar to {@link #error(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, sanitize(arg1), sanitize(arg2));
    }

    /**
     * This method is similar to {@link #error(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, getSanitizedCopy(arguments));
    }

    /**
     * This method is similar to {@link #error(String, Throwable)}
     * method except that the marker data is also taken into
     * consideration.
     *  @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, sanitize(t));
    }
}
