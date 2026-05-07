package com.plovdev.pornviewer.database;

import com.plovdev.pornviewer.commons.models.UserSettings;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class UserSettingsManager {
    private UserSettingsManager() {
        throw new UnsupportedOperationException();
    }

    public static String randomUserId() {
        return UUID.randomUUID().toString();
    }

    @Contract(" -> new")
    public static @NonNull UserSettings getUserSettings() {
        return new UserSettings("0000", "porn365", com.plovdev.pornviewer.commons.models.UserSettings.AppUITheme.WHITE, null, false);
    }
}