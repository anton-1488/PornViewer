package com.plovdev.pornviewer.services.files;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Path;

public record PVFileManager() {
    private static final FileNameMap MIME_MAP = URLConnection.getFileNameMap();
    public static final String PORN_VIEWER = "PornViewer";

    private static final Path PV_BASE_PATH = Path.of(System.getProperty("user.home"), ".PornViewer");
    private static final Path PV_PLUGINS_PATH = PV_BASE_PATH.resolve("plugins");
    private static final Path PV_DOWNLOADS = PV_BASE_PATH.resolve("downloads");
    private static final Path PV_SYSTEM = PV_BASE_PATH.resolve("system");
    private static final Path PV_DB_PATH = PV_SYSTEM.resolve("f9dea68a12f99af0c613e4fdb59756d8f28dd78a608459a775fde1c576bfcfde");

    @NotNull
    public static Path getPVBasePath() {
        return PV_BASE_PATH;
    }

    public static @NonNull Path getPvAdapterPath(String systemPluginId) {
        return PV_PLUGINS_PATH.resolve(systemPluginId + ".pvva");
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