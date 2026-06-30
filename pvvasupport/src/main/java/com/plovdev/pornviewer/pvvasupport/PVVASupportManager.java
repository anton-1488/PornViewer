package com.plovdev.pornviewer.pvvasupport;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.models.adapter.AdapterInfo;
import com.plovdev.pornviewer.database.tables.PVVAProvider;
import com.plovdev.pornviewer.pvvasupport.exceptions.AdapterLoadingException;
import com.plovdev.pornviewer.services.files.PVFileManager;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;

import java.io.IOException;
import java.nio.file.Path;

//TODO check plugin hash when load
public final class PVVASupportManager {
    private static final Cache<String, PVVAHost> CACHED_ADAPTERS = Caffeine.newBuilder()
            .maximumSize(10)
            .build();

    private PVVASupportManager() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        return loadPvvaById(pluginId, false);
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId, boolean isDirect) {
        if (isDirect) {
            return direclyLoadHost(pluginId);
        } else {
            return CACHED_ADAPTERS.get(pluginId, PVVASupportManager::direclyLoadHost);
        }
    }

    private static PVVAHost direclyLoadHost(String pluginId) {
        AdapterInfo info = PVVAProvider.getAdapterById(pluginId);
        Path adapterPath = PVFileManager.getPvAdapterPath(info.pathName());
        try (PVVAReader reader = new DefaultPVVAReader(adapterPath)) {
            return reader.readVideoAdapter();
        } catch (IOException e) {
            throw new AdapterLoadingException("Error load pvva plugin", e);
        }
    }

    public static PVVAHost forceAdapter(String pluginId) {
        CACHED_ADAPTERS.invalidate(pluginId);
        PVVAHost freshHost = direclyLoadHost(pluginId);
        CACHED_ADAPTERS.put(pluginId, freshHost);
        return freshHost;
    }

    @Contract("_ -> param1")
    public static @NonNull PVVAHost forceAdapter(@NonNull PVVAHost host) {
        String pluginId = host.header().getPluginId();
        CACHED_ADAPTERS.invalidate(pluginId);
        CACHED_ADAPTERS.put(pluginId, host);
        return host;
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