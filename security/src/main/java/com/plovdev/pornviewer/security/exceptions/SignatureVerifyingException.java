package com.plovdev.pornviewer.security.exceptions;

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
}