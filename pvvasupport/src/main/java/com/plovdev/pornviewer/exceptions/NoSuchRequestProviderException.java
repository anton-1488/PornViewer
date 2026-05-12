package com.plovdev.pornviewer.exceptions;

import org.plovdev.pvva.models.configs.httpconfig.HttpClientType;

public class NoSuchRequestProviderException extends PornViewerException {
    private HttpClientType clientType;

    public NoSuchRequestProviderException() {
    }

    public NoSuchRequestProviderException(HttpClientType clientType) {
        this.clientType = clientType;
    }

    public NoSuchRequestProviderException(String message) {
        super(message);
    }

    public NoSuchRequestProviderException(String message, HttpClientType type) {
        super(message);
        this.clientType = type;
    }

    public NoSuchRequestProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchRequestProviderException(Throwable cause) {
        super(cause);
    }

    public NoSuchRequestProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HttpClientType getClientType() {
        return clientType;
    }
}