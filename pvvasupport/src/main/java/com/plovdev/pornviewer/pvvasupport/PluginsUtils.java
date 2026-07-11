package com.plovdev.pornviewer.pvvasupport;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.tables.VerifiedHashes;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginSavingException;
import com.plovdev.pornviewer.security.DigestUtils;
import com.plovdev.pornviewer.services.files.PVFileManager;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PluginsUtils {
    private PluginsUtils() {
    }

    public static void saveDownloadedPlugin(String pluginId, byte @NonNull [] pluginData) {
        if (pluginData.length < 64) {
            throw new IllegalArgumentException("Plugin data too short: " + pluginData.length);
        }

        try {
            Path pluginPath = PVFileManager.getPvAdapterPath(pluginId);
            Files.write(pluginPath, pluginData);
            VerifiedHashes.addVerifiedHash(new VerifiedHash(pluginId, VerifiedHash.HashedFileType.PLUGIN, pluginPath, DigestUtils.sha256(pluginData)));
        } catch (IOException e) {
            throw new PluginSavingException(e);
        }
    }
}