package com.plovdev.pornviewer.pvvasupport.exceptions;

import com.plovdev.pornviewer.exceptions.PornViewerSecurityException;

public class PluginNotVerifiedException extends PornViewerSecurityException {
    public PluginNotVerifiedException() {
    }

    public PluginNotVerifiedException(String message) {
        super(message);
    }

    public PluginNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginNotVerifiedException(Throwable cause) {
        super(cause);
    }

    public PluginNotVerifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}