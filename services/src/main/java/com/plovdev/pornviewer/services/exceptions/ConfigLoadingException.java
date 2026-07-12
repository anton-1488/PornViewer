package com.plovdev.pornviewer.services.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class ConfigLoadingException extends PornViewerException {
    public ConfigLoadingException() {
    }

    public ConfigLoadingException(String message) {
        super(message);
    }

    public ConfigLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigLoadingException(Throwable cause) {
        super(cause);
    }

    public ConfigLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}