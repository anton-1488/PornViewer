package com.plovdev.pornviewer.security.exceptions;

public class PornViewerSecurityException extends SecurityException {
    public PornViewerSecurityException() {
    }

    public PornViewerSecurityException(String s) {
        super(s);
    }

    public PornViewerSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public PornViewerSecurityException(Throwable cause) {
        super(cause);
    }
}