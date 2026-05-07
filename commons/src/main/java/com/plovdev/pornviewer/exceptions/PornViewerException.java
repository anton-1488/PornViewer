package com.plovdev.pornviewer.exceptions;

public class PornViewerException extends RuntimeException {
    public PornViewerException() {
    }

    public PornViewerException(String message) {
        super(message);
    }

    public PornViewerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PornViewerException(Throwable cause) {
        super(cause);
    }

    public PornViewerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}