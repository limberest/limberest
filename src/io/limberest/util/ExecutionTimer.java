package io.limberest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enablement should be checked by caller.
 */
public class ExecutionTimer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionTimer.class);

    /**
     * Milliseconds or nanoseconds.
     */
    public enum Precision {
        MS,
        NS;

        @Override
        public String toString() {
            if (name().equals(NS))
                return "ns";
            else
                return "ms";
        }
    }

    /**
     * Debug or Trace.
     */
    public enum LogLevel {
        Error,
        Warn,
        Info,
        Debug,
        Trace
    }

    private Precision precision;
    private LogLevel logLevel;
    private long start;

    public ExecutionTimer() {
        this(Precision.MS, LogLevel.Debug, false);
    }

    public ExecutionTimer(Precision precision) {
        this(precision, LogLevel.Debug, false);
    }

    public ExecutionTimer(LogLevel logLevel) {
        this(Precision.MS, logLevel, false);
    }

    public ExecutionTimer(boolean start) {
        this(Precision.MS, LogLevel.Debug, start);
    }

    public ExecutionTimer(Precision precision, LogLevel logLevel) {
        this(precision, logLevel, false);
    }

    public ExecutionTimer(Precision precision, boolean start) {
        this(precision, LogLevel.Debug, start);
    }

    public ExecutionTimer(LogLevel logLevel, boolean start) {
        this(Precision.MS, logLevel, start);
    }

    public ExecutionTimer(Precision precision, LogLevel logLevel, boolean start) {
        this.precision = precision;
        this.logLevel = logLevel;
        if (start) {
            start();
        }
    }

    public boolean isEnabled() {
        switch (logLevel) {
        case Trace:
            return logger.isTraceEnabled();
        case Info:
            return logger.isInfoEnabled();
        case Warn:
            return logger.isWarnEnabled();
        case Error:
            return logger.isErrorEnabled();
        default:
            return logger.isDebugEnabled();
        }
    }

    public void start() {
        if (precision == Precision.NS)
            start = System.nanoTime();
        else
            start = System.currentTimeMillis();
    }

    public void logAndRestart(String msg) {
        long started = start;
        if (precision == Precision.NS) {
            log(msg, (start = System.nanoTime()) - started);
        }
        else {
            log(msg, (start = System.currentTimeMillis()) - started);
        }
    }

    public void log(String msg) {
        if (precision == Precision.NS) {
            log(msg, System.nanoTime() - start);
        }
        else {
            log(msg, System.currentTimeMillis() - start);
        }
    }

    protected void log(String msg, long elapsed) {
        switch (logLevel) {
        case Trace:
            logger.trace(buildMessage(msg, elapsed));
            break;
        case Info:
            logger.info(buildMessage(msg, elapsed));
            break;
        case Warn:
            logger.warn(buildMessage(msg, elapsed));
            break;
        case Error:
            logger.error(buildMessage(msg, elapsed));
            break;
        default:
            logger.debug(buildMessage(msg, elapsed));
        }
    }

    /**
     * TODO: more flexibility in formatting
     */
    protected String buildMessage(String msg, long elapsed) {
        return new StringBuilder(msg).append(" ").append(elapsed).append(" ").append(precision).toString();
    }
}
