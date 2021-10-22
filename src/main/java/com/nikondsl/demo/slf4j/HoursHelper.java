package com.nikondsl.demo.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class HoursHelper<T,R> {
    private static Logger LOGGER_FIELD = LoggerFactory.getLogger(SecondsToMinutesUtils.class);

    public R process(Function<T,R> function, T argument) {
        try {
            return function.apply(argument);
        } catch (Exception ex) {
            LOGGER_FIELD.error("Could not get result of function '{}' on arg '{}'",
                    function.getClass().getCanonicalName(), argument, ex);
            throw ex;
        }
    }
}
