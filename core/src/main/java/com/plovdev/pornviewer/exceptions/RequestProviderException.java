package com.plovdev.pornviewer.exceptions;

public class RequestProviderException extends PornViewerException {
    public RequestProviderException() {
    }

    public RequestProviderException(String message) {
        super(message);
    }

    public RequestProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestProviderException(Throwable cause) {
        super(cause);
    }

    public RequestProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}