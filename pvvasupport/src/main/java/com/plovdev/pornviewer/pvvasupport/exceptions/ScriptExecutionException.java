package com.plovdev.pornviewer.pvvasupport.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class ScriptExecutionException extends PornViewerException {
    public ScriptExecutionException() {
    }

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptExecutionException(Throwable cause) {
        super(cause);
    }

    public ScriptExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}