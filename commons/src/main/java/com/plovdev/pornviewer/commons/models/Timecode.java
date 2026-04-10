package com.plovdev.pornviewer.commons.models;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public record Timecode(Duration time, String text) {
    @Override
    @NotNull
    public String toString() {
        return String.format("[%s] - %s", time.toString(), text);
    }
}