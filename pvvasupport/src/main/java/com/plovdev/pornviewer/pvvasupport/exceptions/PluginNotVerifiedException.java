package com.plovdev.pornviewer.pvvasupport.exceptions;

import com.plovdev.pornviewer.security.exceptions.PornViewerSecurityException;

public class PluginNotVerifiedException extends PornViewerSecurityException {
    public PluginNotVerifiedException() {
    }

    public PluginNotVerifiedException(String s) {
        super(s);
    }

    public PluginNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginNotVerifiedException(Throwable cause) {
        super(cause);
    }
}