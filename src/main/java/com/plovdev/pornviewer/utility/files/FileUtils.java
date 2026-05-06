package com.plovdev.pornviewer.utility.files;

import com.plovdev.pornviewer.encryptionsupport.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class FileUtils {
    public static final String PORN_VIEWER_SIGN = "b29a674cce9b3fff1010a658070c8933";
    public static final String PV_DOWNLOADS = "downloads/";
    public static final String PV_SYSTEM = "system/";
    public static final String PV_DB_PATH = DigestUtils.sha256("pornviewer.db");

    public final String PV_BASE_PATH;

    public FileUtils() {
        PV_BASE_PATH = System.getProperty("pv.home", System.getProperty("user.home") + "/.PornViewer/");
    }

    public String getPVBasePath() {
        return PV_BASE_PATH;
    }
    @NotNull
    public Path getPvDownloadsPath() {
        return Path.of(getPVBasePath() + PV_DOWNLOADS);
    }
    @NotNull
    public String getPvSystemPath() {
        return getPVBasePath() + PV_SYSTEM;
    }
    @NotNull
    public String getPvDbPath() {
        return getPvSystemPath() + PV_DB_PATH;
    }
    @NotNull
    public String getPVJDBCPathProtocol() {
        return "jdbc:sqlite:" + getPvDbPath();
    }
}