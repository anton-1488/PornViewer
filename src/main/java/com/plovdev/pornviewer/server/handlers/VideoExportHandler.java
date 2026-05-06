package com.plovdev.pornviewer.server.handlers;

import com.google.gson.JsonObject;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader;
import com.plovdev.pornviewer.server.utils.ContentUtils;
import com.plovdev.pornviewer.server.utils.VideoRequestSet;
import com.plovdev.pornviewer.utility.json.JSONSerializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VideoExportHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(VideoExportHandler.class);

    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException {
        Map<String, String> params = parseRequest(exchange.getRequestURI().getQuery());
        String token = params.get("token");
        if (token == null) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            byte[] bodyBytes;
            try (InputStream is = exchange.getRequestBody()) {
                bodyBytes = is.readAllBytes();
            }

            String body = new String(bodyBytes, StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) {
                sendResponse(exchange, 400, "Empty body");
                return;
            }

            JsonObject exportObject = JSONSerializer.deserialize(body, JsonObject.class);
            String from = exportObject.get("from").getAsString();
            String to = exportObject.get("to").getAsString();

            File fromFile = new File(URI.create(from));
            File toFile = new File(to);
            try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(toFile))) {
                VideoRequestSet set = SafeHttpHandler.getCachedOrCreateSet(fromFile);
                ContentUtils.sendDecryptedRange(fromFile, 0, calculateContentLength(set, fromFile, exchange), os, set);
                sendResponse(exchange, 200, formJsonInfo());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Internal Server Error");
                log.error("Error export video: ", e);
            }
        } catch (Exception e) {
            log.error("Error to export video: ", e);
            sendResponse(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    private long calculateContentLength(@NonNull VideoRequestSet set, @NonNull File file, HttpExchange exchange) throws IOException {
        long end = set.getEncryptedVideo().getVideoHeader().plainVideoSize();
        long realStart = VideoHeader.HEADER_SIZE;

        long realEnd = realStart + end;
        if (realStart >= file.length()) {
            exchange.sendResponseHeaders(416, -1);
            return 0;
        }

        if (realEnd >= file.length()) {
            realEnd = file.length() - 1;
        }

        return realEnd - realStart + 1;
    }

    private void sendResponse(@NonNull HttpExchange exchange, int rCode, @NonNull String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(rCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseRequest(String request) {
        Map<String, String> params = new HashMap<>();
        String[] strings = request.split("&");

        for (String param : strings) {
            int firstEq = param.indexOf("=");
            if (firstEq > 0) {
                String name = param.substring(0, firstEq);
                String value = param.substring(firstEq + 1);
                params.put(name, value);
            }
        }
        return params;
    }

    private String formJsonInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("exported", true);
        return JSONSerializer.GSON.toJson(info);
    }

    private boolean checkMethod(String method) {
        return "POST".equals(method);
    }
}