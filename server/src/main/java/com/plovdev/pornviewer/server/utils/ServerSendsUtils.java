package com.plovdev.pornviewer.server.utils;

import com.plovdev.pornviewer.server.models.BasicServerResponse;
import com.plovdev.pornviewer.services.json.JSONSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServerSendsUtils {
    private ServerSendsUtils() {
    }

    public static void sendMessage(@NonNull HttpExchange exchange, int statusCode, Object response) throws IOException {
        String message;
        if (response instanceof String str) {
            message = str;
        } else {
            message = JSONSerializer.serialize(response);
        }

        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public static void sendJson(@NonNull HttpExchange exchange, String msg) throws IOException {
        sendMessage(exchange, 200, msg);
    }

    public static void send401(@NonNull HttpExchange exchange) throws IOException {
        sendMessage(exchange, 401, new BasicServerResponse(401, "Unauthorized"));
    }

    public static void send403(@NonNull HttpExchange exchange) throws IOException {
        sendMessage(exchange, 403, new BasicServerResponse(403, "Access Denied"));
    }

    public static void send405(@NonNull HttpExchange exchange) throws IOException {
        sendMessage(exchange, 405, new BasicServerResponse(405, "Method Not Allowed Here"));
    }

    public static void send500(@NonNull HttpExchange exchange) throws IOException {
        sendMessage(exchange, 500, new BasicServerResponse(500, "Internal Server Error"));
    }
}