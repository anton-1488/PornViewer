package com.plovdev.pornviewer.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.plovdev.pornviewer.utility.files.EnvReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UtilsHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(UtilsHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException {
        Map<String, String> params = parseRequest(exchange.getRequestURI().getQuery());
        String token = params.get("token");
        if (token == null) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        log.info("Checking info. Method: {}", method);
        if (!checkMethod(method)) return;

        String info = formJsonInfo();
        exchange.sendResponseHeaders(200, info.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(info.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }
    private String formJsonInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("version", EnvReader.getEnv("VERSION"));
        return GSON.toJson(info);
    }
    private boolean checkMethod(String method) {
        return "GET".equals(method);
    }

    private @NonNull Map<String, String> parseRequest(@NonNull String request) {
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
}
