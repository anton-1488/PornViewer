package com.plovdev.pornviewer.server.utils;

import com.plovdev.pornviewer.pvvfsupport.read.PVVFVideoReader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.security.RegisteredSecurityModule;
import com.plovdev.pornviewer.server.models.CalculatedVideoParams;
import com.plovdev.pornviewer.server.models.RequestedChunk;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import com.plovdev.pornviewer.services.files.PVFileManager;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.Contract;
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

public class RequetUtils {
    private static final Logger log = LoggerFactory.getLogger(RequetUtils.class);

    @Contract("_, _, _, _ -> new")
    public static @NonNull CalculatedVideoParams calculateVideoRequestParameters(HttpExchange exchange, @NonNull RequestedChunk chunk, @NonNull VideoRequestSet requestSet, long fileLength) throws IOException {
        long start = chunk.start();
        long end = chunk.end();

        EncryptedVideo video = requestSet.encryptedVideo();
        VideoHeader header = video.getVideoHeader();
        VideoMetadata metadata = video.getVideoMetadata();

        long metadataSize = metadata.metadataSize();
        long videoStart = VideoHeader.HEADER_SIZE + header.mimeLength();
        long encVideoLength = header.encVideoSize();

        if (end >= encVideoLength) {
            end = encVideoLength - 1;
        }

        long realStart = videoStart + start;
        long realEnd = videoStart + end;

        if (realStart >= fileLength || realStart >= (videoStart + encVideoLength)) {
            exchange.sendResponseHeaders(416, -1);
            throw new IllegalArgumentException("Illegal position");
        }

        if (realEnd >= fileLength) {
            realEnd = fileLength - 1;
        }

        long contentLength = realEnd - realStart + 1;
        long realContentSize = header.plainVideoSize();

        return new CalculatedVideoParams(header.mime(), start, end, metadataSize, videoStart, encVideoLength, realStart, realEnd, contentLength, realContentSize);
    }

    public static @NonNull VideoRequestSet loadEncryptedVideo(File file) {
        EncryptedVideo video = PVVFVideoReader.readVideo(file);
        CryptoEngine engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(RegisteredSecurityModule.PVVF_SUPPORT), video.getVideoHeader().baseNonce());

        return new VideoRequestSet(video, engine);
    }

    public static @NotNull File checkFile(HttpExchange exchange, String filePath) throws IOException {
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