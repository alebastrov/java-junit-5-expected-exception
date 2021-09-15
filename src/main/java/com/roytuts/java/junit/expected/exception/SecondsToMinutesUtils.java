package com.roytuts.java.junit.expected.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondsToMinutesUtils {
    private static Logger LOG = LoggerFactory.getLogger(SecondsToMinutesUtils.class);

    public int secsToMins(String seconds) {
        try {
            return secsToMins(Integer.parseInt(seconds));
        } catch (NumberFormatException nfe) {
            LOG.error("Could not parse '{}' as integer", seconds, nfe);
            throw nfe;
        }
    }

    public int secsToMins(int seconds) {
        LOG.info("before method");
        try {
            if (seconds < 0) {
                LOG.error("throwing exception");
                throw new IllegalArgumentException("seconds (" + seconds + ") cannot be 0 or negative");
            }
            LOG.info("returning result");
            return Long.valueOf((long) (seconds / 60.0)).intValue();
        } finally {
            LOG.info("after method");
        }
    }

}
