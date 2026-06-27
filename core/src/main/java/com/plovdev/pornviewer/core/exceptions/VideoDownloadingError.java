package com.plovdev.pornviewer.core.exceptions;

public class VideoDownloadingError extends PornViewerException {
    public VideoDownloadingError() {
    }

    public VideoDownloadingError(String message) {
        super(message);
    }

    public VideoDownloadingError(String message, Throwable cause) {
        super(message, cause);
    }

    public VideoDownloadingError(Throwable cause) {
        super(cause);
    }

    public VideoDownloadingError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}