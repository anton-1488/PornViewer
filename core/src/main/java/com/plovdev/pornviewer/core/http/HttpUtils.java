package com.plovdev.pornviewer.core.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class HttpUtils {
    private static final Gson HTTP_BODY_JSON = new GsonBuilder().disableHtmlEscaping().create();

    private HttpUtils() {
    }

    public static @NonNull String formatRequestBody(HttpMethod method, Map<String, Object> body) {
        String foramttedBody = "";
        if (body != null && !body.isEmpty()) {
            if (method == HttpMethod.POST) {
                foramttedBody = HTTP_BODY_JSON.toJson(body);
            } else {
                Map<String, Object> filteredData = body.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                foramttedBody = filteredData.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
            }
        }

        return foramttedBody;
    }

    @Contract(pure = true)
    public static @NonNull @Unmodifiable Map<String, Object> parseResponseBody(HttpMethod method, String body) {
        if (method == HttpMethod.POST) {
            return HTTP_BODY_JSON.fromJson(body, new TypeToken<>() {
            });
        } else {
            if (body.contains("?")) {
                body = body.substring(body.lastIndexOf('?') + 1); // to query string(abc=123&bca=321)
                return Arrays.stream(body.split("&")).collect(Collectors.toUnmodifiableMap(k -> k.substring(0, k.indexOf('=')), v -> v.substring(v.indexOf('=') + 1)));
            } else {
                return Map.of();
            }
        }
    }
}