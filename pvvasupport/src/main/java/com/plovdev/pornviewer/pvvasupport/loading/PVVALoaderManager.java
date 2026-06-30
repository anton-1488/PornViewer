package com.plovdev.pornviewer.pvvasupport.loading;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.models.adapter.AdapterInfo;
import com.plovdev.pornviewer.database.tables.PVVAProvider;
import com.plovdev.pornviewer.services.files.EnvReader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class PVVALoaderManager {
    private static final String MAX_SIZE_KEY = "pornviewer.memory.cache-control.max-adapters-loaded.size";
    private static final String HTTP_PATHS_CONFIG = "/http-paths.properties";
    private static final Cache<String, PVVAHost> CACHED_ADAPTERS = Caffeine.newBuilder()
            .maximumSize(Integer.getInteger(MAX_SIZE_KEY, 10))
            .build();
    private static final PluginLoader PLUGIN_LOADER = new PluginLoaderImpl();

    private PVVALoaderManager() {
        throw new UnsupportedOperationException();
    }

    public static @NonNull CompletableFuture<PVVAHost> downloadPlugin(String pluginId) {
        return CompletableFuture.supplyAsync(() -> {
            EnvReader reader = new EnvReader(HTTP_PATHS_CONFIG);
            String baseUrl = reader.getEnv("base.url");
            String endpoint = reader.getEnv("get-adapter.url") + "?pluginId=" + pluginId;
            PVVAHost downloadedHost = PLUGIN_LOADER.loadFromServer(URI.create(baseUrl + endpoint));
            CACHED_ADAPTERS.put(pluginId, downloadedHost);
            return downloadedHost;
        });
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        return CACHED_ADAPTERS.get(pluginId, PVVALoaderManager::directlyLoadHost);
    }

    private static PVVAHost directlyLoadHost(String pluginId) {
        AdapterInfo info = PVVAProvider.getAdapterById(pluginId);
        return PLUGIN_LOADER.loadFromDisk(Path.of(info.pathName()));
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