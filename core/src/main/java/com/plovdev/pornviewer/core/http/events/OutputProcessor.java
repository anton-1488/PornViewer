package com.plovdev.pornviewer.core.http.events;

public interface OutputProcessor {
    void onProcess(byte[] chunk);
}