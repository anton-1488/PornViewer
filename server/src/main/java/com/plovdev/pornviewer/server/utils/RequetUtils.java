package com.plovdev.pornviewer.server.utils;

import com.plovdev.pornviewer.pvvfsupport.read.PVVFVideoReader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.security.RegisteredSecurityModule;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import com.plovdev.pornviewer.services.files.PVFileManager;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequetUtils {
    private static final Logger log = LoggerFactory.getLogger(RequetUtils.class);

    public static @NonNull VideoRequestSet loadEncryptedVideo(File file) {
        EncryptedVideo video = PVVFVideoReader.readVideo(file);
        CryptoEngine engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(RegisteredSecurityModule.PVVF_SUPPORT), video.getVideoHeader().baseNonce());

        return new VideoRequestSet(video, engine);
    }

    public static @NotNull File checkFile(HttpExchange exchange, @NotNull Map<String, Object> params) throws IOException {
        String filePath = (String) params.get("file");
        if (filePath == null) {
            exchange.sendResponseHeaders(400, -1);
            log.info("File parameter is null");
            throw new NullPointerException("File parameter is missing");
        }

        String decodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
        File file = new File(buildFilePath(decodedPath));
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, -1);
            log.info("File {} not found", file.getAbsolutePath());
            throw new FileNotFoundException("File not found: " + decodedPath);
        }

        String canonicalPath = file.getCanonicalPath();
        String basePath = PVFileManager.getPvDownloadsPath().toFile().getCanonicalPath();
        if (!canonicalPath.startsWith(basePath)) {
            exchange.sendResponseHeaders(403, -1);
            log.warn("Directory traversal attempt: {}", decodedPath);
            throw new SecurityException("Access denied");
        }

        return file;
    }

    private static @NonNull String buildFilePath(String name) {
        return PVFileManager.getPvDownloadsPath().resolve(name).toString();
    }
}