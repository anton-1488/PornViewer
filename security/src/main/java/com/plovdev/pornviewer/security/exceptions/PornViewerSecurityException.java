package com.plovdev.pornviewer.security.exceptions;

public class PornViewerSecurityException extends SecurityException {
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
}