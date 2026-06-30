package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.models.adapter.AdapterInfo;
import com.plovdev.pornviewer.database.tables.PVVAProvider;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;

import java.nio.file.Path;

public final class PVVALoaderManager {
    private static final Cache<String, PVVAHost> CACHED_ADAPTERS = Caffeine.newBuilder()
            .maximumSize(10)
            .build();
    private static final PluginLoader PLUGIN_LOADER = new PluginLoaderImpl();

    private PVVALoaderManager() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        return loadPvvaById(pluginId, false);
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId, boolean isDirect) {
        if (isDirect) {
            return direclyLoadHost(pluginId);
        } else {
            return CACHED_ADAPTERS.get(pluginId, PVVALoaderManager::direclyLoadHost);
        }
    }

    private static PVVAHost direclyLoadHost(String pluginId) {
        AdapterInfo info = PVVAProvider.getAdapterById(pluginId);
        return PLUGIN_LOADER.loadFromDisk(Path.of(info.pathName()));
    }

    public static PVVAHost forceAdapter(String pluginId) {
        CACHED_ADAPTERS.invalidate(pluginId);
        PVVAHost freshHost = direclyLoadHost(pluginId);
        CACHED_ADAPTERS.put(pluginId, freshHost);
        return freshHost;
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