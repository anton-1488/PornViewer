package com.plovdev.pornviewer.core.models.adapter;

import java.net.URI;
import java.util.Objects;

public record PluginInfo(String pluginId,
                         String systemPluginId,
                         String developerId,
                         String author,
                         URI authorPage,
                         URI license,
                         URI homepage,
                         String title,
                         String version,
                         String description,
                         String imageString,
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