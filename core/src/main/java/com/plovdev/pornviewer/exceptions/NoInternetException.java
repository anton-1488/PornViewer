package com.plovdev.pornviewer.exceptions;

public class NoInternetException extends RequestProviderException {
    public static final String ERR_MESSAGE = "Check your intenrent connection";

    public NoInternetException() {
    }

    public NoInternetException(String message) {
        super(message);
    }

    public NoInternetException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoInternetException(Throwable cause) {
        super(cause);
    }

    public NoInternetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}