package com.plovdev.pornviewer.utility;

import com.google.gson.Gson;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.AppInfo;
import com.plovdev.pornviewer.utility.files.EnvReader;
import com.plovdev.pornviewer.utility.files.ServerPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class LauncherHelper {
    private static final Logger log = LoggerFactory.getLogger(LauncherHelper.class);
    private static volatile LauncherHelper INSTANCE = null;
    private final PBPornHandler handler = new PBPornHandler();
    private static final Gson GSON = new Gson();

    public static LauncherHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (LauncherHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LauncherHelper();
                }
            }
        }
        return INSTANCE;
    }

    private LauncherHelper() {
    }

    public boolean checkPrimaryApp() {
        try {
            AppInfo info = GSON.fromJson(handler.requestPorn(ServerPaths.getInstance().getInfoUrl()), AppInfo.class);
            log.info("Info: {}", info);
            if (info.getVersion().equals(EnvReader.getEnv("VERSION"))) {
                return false;
            }
        } catch (Exception e) {
            log.error("Checking error: ", e);
        }
        return true;
    }

    public void notifyDeepLink(URI link) {
        log.info("Notifing deeplink: {}", link);
        String response = handler.executePost(ServerPaths.getInstance().getDeeplinkUrl(), link.toString());
        log.info("Notified DL: {}", response);
    }
}