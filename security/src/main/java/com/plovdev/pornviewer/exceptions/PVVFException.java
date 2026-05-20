package com.plovdev.pornviewer.exceptions;

public class PVVFException extends PornViewerException {
    public PVVFException() {
    }

    public PVVFException(String message) {
        super(message);
    }

    public PVVFException(String message, Throwable cause) {
        super(message, cause);
    }

    public PVVFException(Throwable cause) {
        super(cause);
    }

    public PVVFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}