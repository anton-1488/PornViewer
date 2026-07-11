package com.plovdev.pornviewer.pvvasupport.loading.validator;

import com.plovdev.pornviewer.core.models.app.AppInfo;
import com.plovdev.pornviewer.services.files.EnvReader;
import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.PlatformNotSupportedException;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.utils.VersionUtils;

public final class PluginValidator {
    private PluginValidator() {
    }

    public static void validatePlugin(@NonNull PVVAHost host) {
        AppInfo info = EnvReader.loadAppInfo();
        int appVersion = VersionUtils.versionToInt(info.version());

        PVVAHeader header = host.header();
        int minAppVersion = header.getMinAppVersion();
        int maxAppVersion = header.getMaxAppVersion();

        if (VersionUtils.isCompatibleWithAppVersion(minAppVersion, maxAppVersion, appVersion)) {
            throw new PlatformNotSupportedException("Plugin " + host.getSystemPluginId() + " not support this app version" + appVersion);
        }
    }
}