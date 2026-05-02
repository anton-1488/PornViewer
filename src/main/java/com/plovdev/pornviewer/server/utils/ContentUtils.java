package com.plovdev.pornviewer.server.utils;

import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoMetadata;
import com.plovdev.pornviewer.server.decryptionimpl.ServerDecryptedStreamer;
import com.plovdev.pornviewer.utility.files.FileUtils;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ContentUtils {
    private static final Logger log = LoggerFactory.getLogger(ContentUtils.class);

    public static void sendFileRange(HttpExchange exchange, Chunk chunk, File file, boolean needDecrypt, VideoRequestSet set) throws Exception {
        long start = chunk.getStart();
        long end = chunk.getEnd();

        EncryptedVideo video = set.getEncryptedVideo();

        VideoHeader header = video.getVideoHeader();
        VideoMetadata metadata = video.getVideoMetadata();

        long metadataSize = metadata.metadataSize();

        // 2. Видео начинается после 42-байтового заголовка
        long videoStart = VideoHeader.HEADER_SIZE;
        long encVideoLength = header.encVideoSize();

        if (end >= encVideoLength) {
            end = encVideoLength - 1;
        }

        // 3. Корректируем позиции для отправки клиенту
        long realStart = videoStart + start;
        long realEnd = videoStart + end;

        if (realStart >= file.length() || realStart >= (videoStart + encVideoLength)) {
            exchange.sendResponseHeaders(416, -1);
            return;
        }

        if (realEnd >= file.length()) {
            realEnd = file.length() - 1;
        }
        long contentLength = realEnd - realStart + 1;
        long realContentSize = header.plainVideoSize();

        exchange.getResponseHeaders().set("Content-Type", "video/mp4");
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("Keep-Alive", "timeout=600");
        exchange.getResponseHeaders().set("Content-Range", String.format("bytes %d-%d/%d", start, end, realContentSize));
        exchange.sendResponseHeaders(206, contentLength);

        try (BufferedOutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            if (needDecrypt) {
                sendDecryptedRange(file, video, start, contentLength, os, set);
            } else {
                sendPlainRange(file, realStart, contentLength, os);
            }
        } catch (Exception e) {
            log.debug("Error to process chunk request: {}", e.getMessage());
        }
    }

    private static void sendPlainRange(File file, long start, long length, OutputStream os) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(start);
            byte[] buffer = new byte[VideoChunk.PLAIN_CHUNK_SIZE];
            long remaining = length;
            int bytesRead;

            while (remaining > 0 && (bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                os.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            os.flush();
        }
    }

    public static void sendDecryptedRange(File file, EncryptedVideo video, long startInFile, long length, BufferedOutputStream os, VideoRequestSet set) {
        ServerDecryptedStreamer streamer = new ServerDecryptedStreamer(file,video,  startInFile, length, set);
        streamer.transferToOutput(os);
    }


    public static @NotNull File checkFile(HttpExchange exchange, @NotNull Map<String, String> params) throws Exception {
        String filePath = params.get("file");
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
        String basePath = new File(FileUtils.getPvDownloadsPath().toString()).getCanonicalPath();

        if (!canonicalPath.startsWith(basePath)) {
            exchange.sendResponseHeaders(403, -1);
            log.warn("Directory traversal attempt: {}", decodedPath);
            throw new SecurityException("Access denied");
        }

        return file;
    }

    public static String buildFilePath(String name) {
        return FileUtils.getPvDownloadsPath() + (File.separatorChar + name);
    }
}