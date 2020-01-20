package com.kenvix.android.exception;

public class EnvironmentNotSatisfiedException extends RuntimeException {
    public EnvironmentNotSatisfiedException() {
    }

    public EnvironmentNotSatisfiedException(String message) {
        super(message);
    }

    public EnvironmentNotSatisfiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnvironmentNotSatisfiedException(Throwable cause) {
        super(cause);
    }

    public EnvironmentNotSatisfiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
