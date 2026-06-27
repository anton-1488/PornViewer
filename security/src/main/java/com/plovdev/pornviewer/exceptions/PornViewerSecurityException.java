package com.plovdev.pornviewer.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class PornViewerSecurityException extends PornViewerException {
    public PornViewerSecurityException() {
    }

    public PornViewerSecurityException(String message) {
        super(message);
    }

    public PornViewerSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public PornViewerSecurityException(Throwable cause) {
        super(cause);
    }

    public PornViewerSecurityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}