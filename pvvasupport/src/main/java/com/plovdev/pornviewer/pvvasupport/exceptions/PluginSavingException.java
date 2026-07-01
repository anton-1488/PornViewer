package com.plovdev.pornviewer.pvvasupport.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class PluginSavingException extends PornViewerException {
    public PluginSavingException() {
    }

    public PluginSavingException(String message) {
        super(message);
    }

    public PluginSavingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginSavingException(Throwable cause) {
        super(cause);
    }

    public PluginSavingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}