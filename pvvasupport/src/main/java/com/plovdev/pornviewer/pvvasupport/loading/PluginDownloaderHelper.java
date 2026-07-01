package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.plovdev.pornviewer.core.events.DownloadingType;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.PluginDownloadingChannel;
import com.plovdev.pornviewer.core.models.adapter.PluginsListItem;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginLoadingException;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHost;

import java.util.concurrent.CompletableFuture;

public final class PluginDownloaderHelper {
    private static final String HTTP_PATHS_CONFIG = "/http-paths.properties";

    private PluginDownloaderHelper() {
    }

    public static @NonNull CompletableFuture<PVVAHost> startDownloading(@NonNull PluginsListItem plugin, PluginLoader pluginLoader, Cache<String, PVVAHost> cachedPlugins) {
        return CompletableFuture.supplyAsync(() -> {
            String pluginId = plugin.systemPluginId();
            int pluginSize = plugin.pluginSize();
            try {
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, pluginSize, DownloadingType.START));
                PVVAHost downloadedHost = downloadIfNeed(plugin, pluginLoader, cachedPlugins);
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, pluginSize, DownloadingType.END));
                return downloadedHost;
            } catch (Exception e) {
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, e, DownloadingType.ERROR));
                throw new PluginLoadingException("Error to load download plugin", e);
            }
        });
    }

    private static PVVAHost downloadIfNeed(@NonNull PluginsListItem plugin, @NonNull PluginLoader pluginLoader, @NonNull Cache<String, PVVAHost> cachedPlugins) {
        return cachedPlugins.get(plugin.systemPluginId(), (id) -> {
            PVVAHost downloadedHost = pluginLoader.loadFromServer(plugin);
            cachedPlugins.put(id, downloadedHost);
            return downloadedHost;
        });
    }
}