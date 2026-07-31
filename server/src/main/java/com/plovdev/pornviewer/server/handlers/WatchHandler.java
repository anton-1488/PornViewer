package com.plovdev.pornviewer.server.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata;
import com.plovdev.pornviewer.server.handlers.helpers.VideoWatchHelper;
import com.plovdev.pornviewer.server.models.RequestedChunk;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import com.plovdev.pornviewer.server.utils.RequetUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class WatchHandler extends AbstractServerHandler {
    private static final Cache<File, VideoRequestSet> CACHED_VIDEOS = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(100)
            .build();
    private static final String WATCH_VIDEO = "video";
    private static final String WATCH_IMAGE = "image";
    private static final Logger log = LoggerFactory.getLogger(WatchHandler.class);

    @Override
    protected void onRequest(HttpExchange exchange, URI uri, HttpMethod method, Map<String, Object> params) throws IOException {
        String requestedWatchPart = getWatchPath(uri);
        if (requestedWatchPart.equals(WATCH_VIDEO)) {
            File requestedFile = RequetUtils.checkFile(exchange, params);
            VideoRequestSet requestSet = CACHED_VIDEOS.get(requestedFile, RequetUtils::loadEncryptedVideo);

            if (method == HttpMethod.HEAD) {
                log.info("Processing HEAD request. Requested file: {}", requestedFile);
                processHeadRequest(requestedFile, requestSet, exchange);
            } else if (method == HttpMethod.GET) {
                Headers headers = exchange.getRequestHeaders();
                if (headers != null) {
                    List<String> ranges = headers.get("Range");
                    if (ranges != null && !ranges.isEmpty()) {
                        String range = ranges.getFirst();
                        processGetRequest(requestedFile, requestSet, RequestedChunk.parseChunk(range, requestedFile.length()), exchange);
                    } else {
                        processGetRequest(requestedFile, requestSet, RequestedChunk.fullChunk(requestedFile.length() - 1), exchange);
                    }
                } else {
                    processGetRequest(requestedFile, requestSet, RequestedChunk.fullChunk(requestedFile.length() - 1), exchange);
                }
            }
        } else if (requestedWatchPart.equals(WATCH_IMAGE)) {
            System.out.println("Requested encrypted image");
            //TODO: create for image
        } else {
            throw new UnsupportedOperationException("Unsupported watch part" + requestedWatchPart);
        }
    }

    private void processHeadRequest(File requestedFile, @NonNull VideoRequestSet requestSet, @NonNull HttpExchange exchange) throws IOException {
        VideoHeader header = requestSet.encryptedVideo().getVideoHeader();
        long contentLength = header.plainVideoSize();

        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Content-Type", header.mime());
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(contentLength));
        exchange.sendResponseHeaders(200, -1);
    }

    private void processGetRequest(@NonNull File requestedFile, @NonNull VideoRequestSet requestSet, @NonNull RequestedChunk chunk, HttpExchange exchange) throws IOException {
        long start = chunk.start();
        long end = chunk.end();

        EncryptedVideo video = requestSet.encryptedVideo();
        VideoHeader header = video.getVideoHeader();
        VideoMetadata metadata = video.getVideoMetadata();

        long metadataSize = metadata.metadataSize();
        long videoStart = VideoHeader.HEADER_SIZE + header.mimeLength();
        long encVideoLength = header.encVideoSize();
        long fileLength = requestedFile.length();

        if (end >= encVideoLength) {
            end = encVideoLength - 1;
        }

        long realStart = videoStart + start;
        long realEnd = videoStart + end;

        if (realStart >= fileLength || realStart >= (videoStart + encVideoLength)) {
            exchange.sendResponseHeaders(416, -1);
            return;
        }

        if (realEnd >= fileLength) {
            realEnd = fileLength - 1;
        }

        long contentLength = realEnd - realStart + 1;
        long realContentSize = header.plainVideoSize();

        exchange.getResponseHeaders().set("Content-Type", header.mime());
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("Keep-Alive", "timeout=600");
        exchange.getResponseHeaders().set("Content-Range", String.format("bytes %d-%d/%d", start, end, realContentSize));
        exchange.sendResponseHeaders(206, contentLength);

        try (BufferedOutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            VideoWatchHelper.transferEncryptedStream(requestedFile, requestSet, start, contentLength, os);
        }
    }

    private @NonNull String getWatchPath(@NonNull URI uri) {
        String uriStr = uri.toString();
        return uriStr.substring("/watch/".length(), uriStr.indexOf("?"));
    }

    @Override
    protected List<HttpMethod> getSupportedMethods() {
        return List.of(HttpMethod.GET, HttpMethod.HEAD);
    }
}