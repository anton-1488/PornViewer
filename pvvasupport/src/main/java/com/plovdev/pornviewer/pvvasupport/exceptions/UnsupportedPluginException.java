package com.plovdev.pornviewer.pvvasupport.exceptions;

public class UnsupportedPluginException extends PluginLoadingException {
    public UnsupportedPluginException() {
    }

    public UnsupportedPluginException(String message) {
        super(message);
    }

    public UnsupportedPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedPluginException(Throwable cause) {
        super(cause);
    }

    public UnsupportedPluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}