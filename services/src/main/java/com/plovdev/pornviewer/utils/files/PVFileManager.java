package com.plovdev.pornviewer.utils.files;

import com.plovdev.pornviewer.utils.crypto.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public record PVFileManager() {
    public static final String PORN_VIEWER = "PornViewer";
    public static final String PORN_VIEWER_SIGN = DigestUtils.md5("PornViewer");
    private static final String PV_PLUGINS_PATH = "plugins";
    private static final Path PV_BASE_PATH = Path.of(System.getProperty("user.home"), ".PornViewer");
    private static final Path PV_DOWNLOADS = Path.of("downloads");
    private static final Path PV_SYSTEM = Path.of("system");
    private static final Path PV_DB_PATH = Path.of(DigestUtils.sha256("pornviewer.db"));

    @NotNull
    public static Path getPVBasePath() {
        return PV_BASE_PATH;
    }

    public static @NonNull Path getPvAdapterPath(String pathName) {
        String basePath = getPVBasePath().toString();
        return Path.of(basePath, PV_PLUGINS_PATH, pathName);
    }

    @NotNull
    public static Path getPvDownloadsPath() {
        return Path.of(getPVBasePath().toString(), PV_DOWNLOADS.toString());
    }

    @NotNull
    public static Path getPvSystemPath() {
        return Path.of(getPVBasePath().toString(), PV_SYSTEM.toString());
    }

    @NotNull
    public static Path getPvDbPath() {
        return Path.of(getPvSystemPath().toString(), PV_DB_PATH.toString());
    }

    @NotNull
    public static String getPVJDBCPathProtocol() {
        return "jdbc:sqlite:" + getPvDbPath();
    }
}