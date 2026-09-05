package com.plovdev.pornviewer.server.models;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public record ExportVideoRequest(String fileId, String toPath) {
    @Contract(pure = true)
    public @NonNull Path preparedToPath() {
        return Path.of(toPath);
    }
}