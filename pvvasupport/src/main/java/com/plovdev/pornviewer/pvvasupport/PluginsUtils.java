package com.plovdev.pornviewer.pvvasupport;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.tables.VerifiedHashes;
import com.plovdev.pornviewer.security.DigestUtils;

import java.nio.file.Path;

public final class PluginsUtils {
    private PluginsUtils() {
    }

    public static void saveDownloadedPlugin(String pluginId, boolean hasSign, byte[] pluginData) {
        if (hasSign) {
            int pluginWithoutSignLength = pluginData.length - 64;
            byte[] pluginWithoutSign = new byte[pluginWithoutSignLength];
            System.arraycopy(pluginData, 0, pluginWithoutSign, 0, pluginWithoutSignLength);
            pluginData = pluginWithoutSign;
        }

        String pluginPath
        VerifiedHashes.addVerifiedHash(new VerifiedHash(pluginId, VerifiedHash.HashedFileType.PLUGIN, Path.of(""), DigestUtils.sha256(pluginData)));
    }
}