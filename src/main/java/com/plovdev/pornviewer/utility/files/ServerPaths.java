package com.plovdev.pornviewer.utility.files;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServerPaths {
    public static final String SERVER_BASE = "http://127.0.0.1:3535";
    private static final Logger log = LoggerFactory.getLogger(ServerPaths.class);
    private static String token;
    private static volatile ServerPaths INSTANCE = null;

    public static void updateToken(String newToken) {
        token = newToken;
    }

    public static ServerPaths getInstance() {
        Objects.requireNonNull(token);

        if (INSTANCE == null) {
            synchronized (ServerPaths.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServerPaths();
                }
            }
        }
        return INSTANCE;
    }

    private ServerPaths() {
    }

    public String getToken() {
        return token;
    }

    @NotNull
    public String replaceFileToHttpPath(String file) {
        file = file.substring(file.lastIndexOf("/") + 1);
        return String.format(SERVER_BASE + "/video?file=%s&token=%s", URLEncoder.encode(file, StandardCharsets.UTF_8), token);
    }

    public String getInfoUrl() {
        return String.format(SERVER_BASE + "/info?token=%s", token);
    }

    public String getExportUrl() {
        return String.format(SERVER_BASE + "/export?token=%s", token);
    }

    public String getDeeplinkUrl() {
        return String.format(SERVER_BASE + "/deeplink?token=%s", token);
    }
}