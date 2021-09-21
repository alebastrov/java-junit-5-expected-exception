package com.nikondsl.demo.log4j;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class SecondsToMinutesUtils {
    private static Logger LOG = LogManager.getLogger(SecondsToMinutesUtils.class);

    public int secsToMins(String seconds) {
        try {
            if (seconds == null) {
                throw new NullPointerException("Argument cannot be null");
            }
            return secsToMins(Integer.parseInt(seconds));
        } catch (Exception exception) {
            LOG.debug("some", () -> doIt("123"));
            LOG.error("Could not parse '" + seconds + "' as integer", exception);
            throw exception;
        }
    }

    private String doIt(String s) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "break";
        }
        return "wait";
    }

    public int secsToMins(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds (" + seconds + ") cannot be 0 or negative");
        }
        return Long.valueOf((long) (seconds / 60.0)).intValue();
    }
}
