package com.plovdev.pornviewer.core.http;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record PornRequest(@NonNull URI path, @NonNull HttpMethod method, String body) {
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<>();
    private static final Map<String, String> INTERNAL_HEADERS = new HashMap<>();

    static {
        DEFAULT_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        DEFAULT_HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        DEFAULT_HEADERS.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        DEFAULT_HEADERS.put("Upgrade-Insecure-Requests", "1");
        DEFAULT_HEADERS.put("Cache-Control", "max-age=0");

        INTERNAL_HEADERS.put("User-Agent", "PornViewer/2.0");
        INTERNAL_HEADERS.put("Accept", "application/json");
        INTERNAL_HEADERS.put("Accept-Language", "ru,en");
    }

    @Contract(pure = true)
    public static @NonNull Map<String, String> getDefaultHeaders() {
        return Map.copyOf(DEFAULT_HEADERS);
    }

    public static @NonNull Map<String, String> getInternalHeaders() {
        return Map.copyOf(INTERNAL_HEADERS);
    }

    public PornRequest {
        Objects.requireNonNull(path);
        Objects.requireNonNull(method);

        if (method == HttpMethod.POST) {
            Objects.requireNonNull(body);
        }
    }

    @Contract("_ -> new")
    public static @NonNull PornRequest get(String url) {
        return get(URI.create(url));
    }

    @Contract("_ -> new")
    public static @NonNull PornRequest get(URI uri) {
        return new PornRequest(uri, HttpMethod.GET, null);
    }

    @Contract("_ -> new")
    public static @NonNull PornRequest head(String url) {
        return head(URI.create(url));
    }

    @Contract("_ -> new")
    public static @NonNull PornRequest head(URI uri) {
        return new PornRequest(uri, HttpMethod.HEAD, null);
    }

    @Contract("_, _ -> new")
    public static @NonNull PornRequest post(String url, String body) {
        return post(URI.create(url), body);
    }

    @Contract("_, _ -> new")
    public static @NonNull PornRequest post(URI url, String body) {
        return new PornRequest(url, HttpMethod.POST, body);
    }
}