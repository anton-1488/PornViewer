package com.plovdev.pornviewer.exceptions;

public class PornViewerServerException extends PornViewerException {
    public PornViewerServerException(String message) {
        super(message);
    }

    public PornViewerServerException() {
    }

    public PornViewerServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PornViewerServerException(Throwable cause) {
        super(cause);
    }

    public PornViewerServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}