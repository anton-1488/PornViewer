package com.plovdev.pornviewer.commons.models.porn;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public record Timecode(Duration time, String text) {
    public Timecode {
        text = text.trim().strip();
    }

    @Override
    @NotNull
    public String toString() {
        return String.format("[%s] - \"%s\"", time.toString(), text);
    }
}