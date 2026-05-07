package com.plovdev.pornviewer.exceptions;

public class AdapterLoadingException extends PornViewerException {
    public AdapterLoadingException() {
    }

    public AdapterLoadingException(String message) {
        super(message);
    }

    public AdapterLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdapterLoadingException(Throwable cause) {
        super(cause);
    }

    public AdapterLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}