package com.plovdev.pornviewer.pvvasupport.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class PluginLoadingException extends PornViewerException {
    public PluginLoadingException() {
    }

    public PluginLoadingException(String message) {
        super(message);
    }

    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginLoadingException(Throwable cause) {
        super(cause);
    }

    public PluginLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}