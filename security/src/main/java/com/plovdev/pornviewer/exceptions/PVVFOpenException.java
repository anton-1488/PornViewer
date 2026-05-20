package com.plovdev.pornviewer.exceptions;

public class PVVFOpenException extends PVVFException {
    public PVVFOpenException() {
    }

    public PVVFOpenException(String message) {
        super(message);
    }

    public PVVFOpenException(String message, Throwable cause) {
        super(message, cause);
    }

    public PVVFOpenException(Throwable cause) {
        super(cause);
    }

    public PVVFOpenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}