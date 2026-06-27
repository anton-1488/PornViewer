package com.plovdev.pornviewer.database.exceptions;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;

public class PVDataBaseException extends PornViewerException {
    private int code;

    public PVDataBaseException() {
    }

    public PVDataBaseException(int code) {
        this.code = code;
    }

    public PVDataBaseException(String message) {
        super(message);
    }

    public PVDataBaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public PVDataBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PVDataBaseException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public PVDataBaseException(Throwable cause) {
        super(cause);
    }

    public PVDataBaseException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public PVDataBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }
}