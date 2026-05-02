package com.plovdev.pornviewer.httpquering;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record PornRequest(String url, String method, Map<String, String> headers, String body, Duration timeout) {
    private static final Map<String, String> DEFAULT_HEADERS = new TreeMap<>();
    static {
        DEFAULT_HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        DEFAULT_HEADERS.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        DEFAULT_HEADERS.put("Cache-Control", "max-age=0");
        DEFAULT_HEADERS.put("Upgrade-Insecure-Requests", "1");
        DEFAULT_HEADERS.put("Referer", "http://8porno365.info");
        DEFAULT_HEADERS.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
    }
    public PornRequest {
        Objects.requireNonNull(method);
        Objects.requireNonNull(headers);
        if (method.equals("POST")) {
            Objects.requireNonNull(body);
        }
    }

    @Contract("_ -> new")
    public static @NotNull PornRequest get(String url) {
        return new PornRequest(url, "GET", DEFAULT_HEADERS, null, Duration.ofSeconds(30));
    }
    @Contract("_ -> new")
    public static @NotNull PornRequest head(String url) {
        return new PornRequest(url, "HEAD", DEFAULT_HEADERS, null, Duration.ofSeconds(30));
    }

    @Contract("_, _ -> new")
    public static @NotNull PornRequest post(String url, String body) {
        return new PornRequest(url, "POST", DEFAULT_HEADERS, body, Duration.ofSeconds(30));
    }
}