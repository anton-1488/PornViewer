package com.plovdev.pornviewer.core.models.app;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record UserSettings(
        @NonNull String id,
        @NonNull String adapter,
        @NonNull AppUITheme theme,
        @Nullable String appPath,
        boolean loadTrailers) {

    public enum AppUITheme {
        BLACK, WHITE, SYSTEM
    }

    public UserSettings {
        Objects.requireNonNull(id);
        Objects.requireNonNull(adapter);
        Objects.requireNonNull(theme);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserSettings that = (UserSettings) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}