package com.plovdev.pornviewer.utils.files;

import com.plovdev.pornviewer.utils.crypto.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Path;

public record PVFileManager() {
    private static final FileNameMap MIME_MAP = URLConnection.getFileNameMap();
    public static final String PORN_VIEWER = "PornViewer";
    public static final String PORN_VIEWER_SIGN = DigestUtils.md5("PornViewer");
    private static final String PV_PLUGINS_PATH = "plugins";
    private static final Path PV_BASE_PATH = Path.of(System.getProperty("user.home"), ".PornViewer");
    private static final Path PV_DOWNLOADS = Path.of("downloads");
    private static final Path PV_SYSTEM = Path.of("system");
    private static final Path PV_DB_PATH = Path.of("pornviewer.db");

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

    public static @NonNull String guessMimeType(String filename) {
        String mimeType = MIME_MAP.getContentTypeFor(filename);
        if (mimeType == null || mimeType.isEmpty()) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            return switch (ext) {
                case "mkv" -> "MKV ";
                case "avi" -> "AVI ";
                case "mov" -> "MOV ";
                case "webm" -> "WEBM";
                default -> "MP4 ";
            };
        }
        return mimeType;
    }
}