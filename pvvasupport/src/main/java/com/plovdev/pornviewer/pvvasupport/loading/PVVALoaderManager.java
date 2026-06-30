package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.models.adapter.AdapterInfo;
import com.plovdev.pornviewer.database.tables.PVVAProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class PVVALoaderManager {
    private static final String MAX_SIZE_KEY = "pornviewer.memory.cache-control.max-adapters-loaded.size";
    private static final Cache<String, PVVAHost> CACHED_ADAPTERS = Caffeine.newBuilder()
            .maximumSize(Integer.getInteger(MAX_SIZE_KEY, 10))
            .build();
    private static final PluginLoader PLUGIN_LOADER = new PluginLoaderImpl();

    private PVVALoaderManager() {
        throw new UnsupportedOperationException();
    }

    public static @NonNull CompletableFuture<PVVAHost> downloadPlugin(String pluginId) {
        return CompletableFuture.supplyAsync(() -> {
            PluginDownloader downloader = new PluginDownloader();
            PVVAHost downloadedHost = downloader.downloadPlugin(pluginId);
            CACHED_ADAPTERS.put(pluginId, downloadedHost);
            return downloadedHost;
        });
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        return loadPvvaById(pluginId, false);
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId, boolean isDirect) {
        if (isDirect) {
            return direclyLoadHost(pluginId);
        } else {
            return CACHED_ADAPTERS.getIfPresent(pluginId);
        }
    }

    private static PVVAHost direclyLoadHost(String pluginId) {
        AdapterInfo info = PVVAProvider.getAdapterById(pluginId);
        PVVAHost host = PLUGIN_LOADER.loadFromDisk(Path.of(info.pathName()));
        CACHED_ADAPTERS.put(pluginId, host);
        return host;
    }

    public static PVVAHost forceAdapter(String pluginId) {
        CACHED_ADAPTERS.invalidate(pluginId);
        PVVAHost freshHost = direclyLoadHost(pluginId);
        CACHED_ADAPTERS.put(pluginId, freshHost);
        return freshHost;
    }

    public static boolean isCached(String pluginId) {
        return CACHED_ADAPTERS.getIfPresent(pluginId) != null;
    }

    public static void clearCache() {
        CACHED_ADAPTERS.invalidateAll();
    }

    public static void removeFromCache(String pluginId) {
        CACHED_ADAPTERS.invalidate(pluginId);
    }

    public static long getCacheSize() {
        return CACHED_ADAPTERS.estimatedSize();
    }
}