package com.plovdev.pornviewer.server.handlers;

import com.google.gson.JsonObject;
import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.server.handlers.helpers.VideoWatchHelper;
import com.plovdev.pornviewer.server.models.CalculatedVideoParams;
import com.plovdev.pornviewer.server.models.ExportVideoRequest;
import com.plovdev.pornviewer.server.models.RequestedChunk;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import com.plovdev.pornviewer.server.utils.RequetUtils;
import com.plovdev.pornviewer.server.utils.ServerSendsUtils;
import com.plovdev.pornviewer.server.utils.VideoRequestsCache;
import com.plovdev.pornviewer.services.files.PVFileManager;
import com.plovdev.pornviewer.services.json.JSONSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ExportHandler extends AbstractServerHandler {
    @Override
    protected void onRequest(@NonNull HttpExchange exchange, URI uri, HttpMethod method, Map<String, Object> params) throws Exception {
        byte[] bodyBytes;
        try (InputStream is = exchange.getRequestBody()) {
            bodyBytes = is.readAllBytes();
        }

        String body = new String(bodyBytes, StandardCharsets.UTF_8).trim();
        if (body.isEmpty()) {
            ServerSendsUtils.send400(exchange, "Empty body");
            return;
        }

        ExportVideoRequest request = JSONSerializer.deserialize(body, ExportVideoRequest.class);
        checkAndPrepareRequest(request);

        File requestedFile = RequetUtils.checkFile(exchange, request.fileId());
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(request.toPath()))) {
            VideoRequestSet set = VideoRequestsCache.getRequestSet(requestedFile);
            CalculatedVideoParams videoParams = RequetUtils.calculateVideoRequestParameters(exchange, new RequestedChunk(0, set.encryptedVideo().getVideoHeader().plainVideoSize()), set, requestedFile.length());

            VideoWatchHelper.transferEncryptedStream(requestedFile, set, videoParams.realStart(), videoParams.contentLength(), os);
            ServerSendsUtils.sendMessage(exchange, 200, formJsonInfo());
        }
    }

    private void checkAndPrepareRequest(ExportVideoRequest request) throws IOException {
        try (Stream<Path> idsStream = Files.walk(PVFileManager.getPvDownloadsPath())) {
            boolean noHasFile = idsStream.noneMatch(p -> p.getFileName().toString().equals(request.fileId()));
            if (noHasFile) {
                throw new FileNotFoundException("File " + request.fileId() + " not found!");
            }

            Path toPath = request.preparedToPath();
            if (Files.notExists(toPath)) {
                Files.createDirectories(toPath);
            }
        }
    }

    private @NonNull String formJsonInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("exported", true);
        return JSONSerializer.GSON.toJson(info);
    }

    @Override
    protected List<HttpMethod> getSupportedMethods() {
        return List.of(HttpMethod.POST);
    }
}