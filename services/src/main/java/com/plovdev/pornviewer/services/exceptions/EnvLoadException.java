package com.plovdev.pornviewer.services.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class EnvLoadException extends PornViewerException {
    public EnvLoadException() {
    }

    public EnvLoadException(String message) {
        super(message);
    }

    public EnvLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnvLoadException(Throwable cause) {
        super(cause);
    }

    public EnvLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}