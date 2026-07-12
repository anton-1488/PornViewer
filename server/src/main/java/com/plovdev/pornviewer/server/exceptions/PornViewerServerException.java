package com.plovdev.pornviewer.server.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

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