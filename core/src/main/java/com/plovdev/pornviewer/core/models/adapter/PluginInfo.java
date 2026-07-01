package com.plovdev.pornviewer.core.models.adapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Objects;

public record PluginInfo(@NonNull String pluginId,
                         @NonNull String systemPluginId,
                         @NonNull String developerId,
                         @NonNull String title,
                         @NonNull String version,
                         @NonNull String description,
                         @Nullable String author,
                         @Nullable URI authorPage,
                         @Nullable URI license,
                         @Nullable URI homepage,
                         @Nullable String imageString,
                         int minAppVersion,
                         int maxAppVersion,
                         int buildId) {
    public PluginInfo {
        Objects.requireNonNull(pluginId);
        Objects.requireNonNull(systemPluginId);
        Objects.requireNonNull(developerId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(version);
        Objects.requireNonNull(description);
    }
}