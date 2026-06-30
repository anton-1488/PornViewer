package com.plovdev.pornviewer.exceptions;

public class SignatureVerifyingException extends PornViewerSecurityException {
    public SignatureVerifyingException() {
    }

    public SignatureVerifyingException(String message) {
        super(message);
    }

    public SignatureVerifyingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureVerifyingException(Throwable cause) {
        super(cause);
    }

    public SignatureVerifyingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}