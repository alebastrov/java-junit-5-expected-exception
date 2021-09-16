package com.nikondsl.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class HoursHelper<T,R> {
    private static Logger LOG = LoggerFactory.getLogger(SecondsToMinutesUtils.class);

    public R process(Function<T,R> function, T argument) {
        try {
            return function.apply(argument);
        } catch (Exception ex) {
            LOG.error("Could not get result of function '{}' on arg '{}'",
                    function.getClass().getCanonicalName(), argument, ex);
            throw ex;
        }
    }
}
