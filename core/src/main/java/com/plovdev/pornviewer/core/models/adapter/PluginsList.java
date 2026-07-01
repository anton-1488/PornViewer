package com.plovdev.pornviewer.core.models.adapter;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record PluginsList(@SerializedName("count") int count,
                          @SerializedName("plugins-list") @NonNull List<PluginsListItem> pluginsList) {

    @Contract(pure = true)
    @Override
    public @NonNull List<PluginsListItem> pluginsList() {
        return List.copyOf(pluginsList);
    }
}