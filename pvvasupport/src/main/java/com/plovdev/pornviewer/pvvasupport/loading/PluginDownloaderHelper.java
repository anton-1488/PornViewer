package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.plovdev.pornviewer.core.events.DownloadingType;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.PluginDownloadingChannel;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginLoadingException;
import com.plovdev.pornviewer.services.files.EnvReader;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHost;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class PluginDownloaderHelper {
    private static final String HTTP_PATHS_CONFIG = "/http-paths.properties";

    private PluginDownloaderHelper() {
    }

    public static @NonNull CompletableFuture<PVVAHost> startDownloading(String pluginId, int pluginSize, PluginLoader pluginLoader, Cache<String, PVVAHost> cachedPlugins) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, pluginSize, DownloadingType.START));
                PVVAHost downloadedHost = downloadIfNeed(pluginId, pluginLoader, cachedPlugins);
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, pluginSize, DownloadingType.END));
                return downloadedHost;
            } catch (Exception e) {
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, e, DownloadingType.ERROR));
                throw new PluginLoadingException("Error to load download plugin", e);
            }
        });
    }

    private static PVVAHost downloadIfNeed(String pluginId, @NonNull PluginLoader pluginLoader, @NonNull Cache<String, PVVAHost> cachedPlugins) {
        return cachedPlugins.get(pluginId, (id) -> {
            EnvReader reader = new EnvReader(HTTP_PATHS_CONFIG);
            String baseUrl = reader.getEnv("base.url");
            String endpoint = reader.getEnv("get-adapter.url") + "?pluginId=" + id;
            PVVAHost downloadedHost = pluginLoader.loadFromServer(id, URI.create(baseUrl + endpoint));
            cachedPlugins.put(id, downloadedHost);
            return downloadedHost;
        });
    }
}