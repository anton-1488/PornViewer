package com.plovdev.pornviewer.core.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.stream.Collectors;

public final class HttpUtils {
    private static final Gson HTTP_BODY_JSON = new GsonBuilder().disableHtmlEscaping().create();

    private HttpUtils() {
    }

    public static @NonNull String formatRequestBody(HttpMethod method, Map<String, Object> body) {
        String foramttedBody = "";
        if (body != null && !body.isEmpty()) {
            if (method == HttpMethod.GET) {
                Map<String, Object> filteredData = body.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                foramttedBody = filteredData.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
            } else if (method == HttpMethod.POST) {
                foramttedBody = HTTP_BODY_JSON.toJson(body);
            } else {
                throw new IllegalArgumentException("Unsupported http method: " + method);
            }
        }

        return foramttedBody;
    }
}