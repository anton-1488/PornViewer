package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.models.adapter.PluginInfo;
import com.plovdev.pornviewer.core.models.adapter.PluginsListItem;
import com.plovdev.pornviewer.database.tables.PluginsProvider;
import com.plovdev.pornviewer.services.files.PVFileManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;

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

    public static @NonNull CompletableFuture<PVVAHost> downloadPlugin(PluginsListItem pluginToLoad) {
        return PluginDownloaderHelper.startDownloading(pluginToLoad, PLUGIN_LOADER, CACHED_ADAPTERS);
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        return CACHED_ADAPTERS.get(pluginId, PVVALoaderManager::directlyLoadHost);
    }

    private static PVVAHost directlyLoadHost(String pluginId) {
        PluginInfo info = PluginsProvider.getAdapterById(pluginId);
        return PLUGIN_LOADER.loadFromDisk(pluginId, PVFileManager.getPvAdapterPath(info.systemPluginId()));
    }

    public static PVVAHost forceAdapter(String pluginId) {
        CACHED_ADAPTERS.invalidate(pluginId);
        PVVAHost freshHost = directlyLoadHost(pluginId);
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