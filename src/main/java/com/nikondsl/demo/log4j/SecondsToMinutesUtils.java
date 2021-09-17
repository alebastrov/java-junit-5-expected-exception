package com.nikondsl.demo.log4j;


import org.apache.log4j.Logger;

public class SecondsToMinutesUtils {
    private static Logger LOG = Logger.getLogger(SecondsToMinutesUtils.class);

    public int secsToMins(String seconds) {
        try {
            if (seconds == null) {
                throw new NullPointerException("Argument cannot be null");
            }
            return secsToMins(Integer.parseInt(seconds));
        } catch (Exception exception) {
            LOG.error("Could not parse '" + seconds + "' as integer", exception);
            throw exception;
        }
    }

    public int secsToMins(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds (" + seconds + ") cannot be 0 or negative");
        }
        return Long.valueOf((long) (seconds / 60.0)).intValue();
    }
}
