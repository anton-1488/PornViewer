package com.plovdev.pornviewer.services;

import org.jspecify.annotations.NonNull;

public final class VersionUtils {
    private VersionUtils() {
    }

    public static int versionToInt(@NonNull String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        return major * 10000 + minor * 100 + patch;
    }

    public static @NonNull String intToVersion(int versionCode) {
        int major = versionCode / 10000;
        int minor = (versionCode % 10000) / 100;
        int patch = versionCode % 100;

        return String.format("%d.%d.%d", major, minor, patch);
    }

    public static int generateBuildId() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}