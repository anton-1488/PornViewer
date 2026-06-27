package com.plovdev.pornviewer.pvvfsupport.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class PVVFException extends PornViewerException {
    public PVVFException() {
    }

    public PVVFException(String message) {
        super(message);
    }

    public PVVFException(String message, Throwable cause) {
        super(message, cause);
    }

    public PVVFException(Throwable cause) {
        super(cause);
    }

    public PVVFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}