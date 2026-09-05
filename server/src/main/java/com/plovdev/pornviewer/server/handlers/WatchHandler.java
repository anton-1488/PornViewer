package com.plovdev.pornviewer.server.handlers;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.server.handlers.helpers.VideoWatchHelper;
import com.plovdev.pornviewer.server.models.CalculatedVideoParams;
import com.plovdev.pornviewer.server.models.RequestedChunk;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import com.plovdev.pornviewer.server.utils.RequetUtils;
import com.plovdev.pornviewer.server.utils.VideoRequestsCache;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class WatchHandler extends AbstractServerHandler {
    private static final String WATCH_VIDEO = "video";
    private static final String WATCH_IMAGE = "image";
    private static final Logger log = LoggerFactory.getLogger(WatchHandler.class);

    @Override
    protected void onRequest(HttpExchange exchange, URI uri, HttpMethod method, Map<String, Object> params) throws IOException {
        String requestedWatchPart = getWatchPath(uri);
        if (requestedWatchPart.equals(WATCH_VIDEO)) {
            File requestedFile = RequetUtils.checkFile(exchange, (String) params.get("file"));
            VideoRequestSet requestSet = VideoRequestsCache.getRequestSet(requestedFile);

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
            throw new UnsupportedOperationException("Images not supported yet");
        } else {
            throw new UnsupportedOperationException("Unsupported watch part: " + requestedWatchPart);
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

    private void processGetRequest(@NonNull File requestedFile, @NonNull VideoRequestSet requestSet, @NonNull RequestedChunk chunk, @NonNull HttpExchange exchange) throws IOException {
        CalculatedVideoParams params = RequetUtils.calculateVideoRequestParameters(exchange, chunk, requestSet, requestedFile.length());
        long contentLength = params.contentLength();

        exchange.getResponseHeaders().set("Content-Type", params.mime());
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("Keep-Alive", "timeout=600");
        exchange.getResponseHeaders().set("Content-Range", String.format("bytes %d-%d/%d", params.start(), params.end(), params.realContentSize()));
        exchange.sendResponseHeaders(206, contentLength);

        try (BufferedOutputStream os = new BufferedOutputStream(exchange.getResponseBody())) {
            VideoWatchHelper.transferEncryptedStream(requestedFile, requestSet, params.realStart(), contentLength, os);
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