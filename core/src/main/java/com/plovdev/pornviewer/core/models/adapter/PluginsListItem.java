package com.plovdev.pornviewer.core.models.adapter;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.Objects;

public record PluginsListItem(@SerializedName("plugin-id") @NonNull String pluginId,
                              @SerializedName("system-plugin-id") @NonNull String systemPluginId,
                              @SerializedName("developer-id") @NonNull String developerId,
                              @SerializedName("author") String author,
                              @SerializedName("license") URI license,
                              @SerializedName("homepage") URI homepage,
                              @SerializedName("title") @NonNull String title,
                              @SerializedName("version") @NonNull String version,
                              @SerializedName("description") @NonNull String description,
                              @SerializedName("download-url") @NonNull URI downloadUrl,
                              @SerializedName("image-url") URI imageUrl,
                              @SerializedName("plugin-size") int pluginSize,
                              @SerializedName("min-app-version") int minAppVersion,
                              @SerializedName("max-app-version") int maxAppVersion,
                              @SerializedName("build-id") int buildId) {
    public PluginsListItem {
        Objects.requireNonNull(pluginId);
        Objects.requireNonNull(systemPluginId);
        Objects.requireNonNull(developerId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(version);
        Objects.requireNonNull(description);
        Objects.requireNonNull(downloadUrl);
    }
}