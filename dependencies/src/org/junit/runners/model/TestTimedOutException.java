package org.junit.runners.model;

import java.util.concurrent.TimeUnit;

/**
 * Exception thrown when a FabriTest fails on timeout.
 * 
 * @since 4.12
 * 
 */
public class TestTimedOutException extends Exception {

    private static final long serialVersionUID = 31935685163547539L;

    private final TimeUnit timeUnit;
    private final long timeout;

    /**
     * Creates exception with a standard message "FabriTest timed out after [timeout] [timeUnit]"
     * 
     * @param timeout the amount of time passed before the FabriTest was interrupted
     * @param timeUnit the time unit for the timeout value
     */
    public TestTimedOutException(long timeout, TimeUnit timeUnit) {
        super(String.format("FabriTest timed out after %d %s",
                timeout, timeUnit.name().toLowerCase()));
        this.timeUnit = timeUnit;
        this.timeout = timeout;
    }

    /**
     * Gets the time passed before the FabriTest was interrupted
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Gets the time unit for the timeout value
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
