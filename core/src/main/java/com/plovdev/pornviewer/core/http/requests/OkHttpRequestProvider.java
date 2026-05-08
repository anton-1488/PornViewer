package com.plovdev.pornviewer.core.http.requests;

import com.plovdev.pornviewer.core.http.PornRequest;
import okhttp3.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class OkHttpRequestProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(OkHttpRequestProvider.class);
    private OkHttpClient client;
    private final HttpConfig httpConfig;

    public OkHttpRequestProvider(HttpConfig config) {
        client = configureHttpClient(config);
        this.httpConfig = config;
    }

    @Override
    public String executeGet(PornRequest request) {
        try (Response response = client.newCall(configurateRequest(request)).execute()) {
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).string();
            } else {
                log.warn("Non success get response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error execute get: ", e);
        }
        return "";
    }

    @Override
    public byte[] executeRaw(PornRequest request) {
        try (Response response = client.newCall(configurateRequest(request)).execute()) {
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).bytes();
            } else {
                log.warn("Non success raw response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error execute raw: ", e);
        }
        return new byte[0];
    }

    @Override
    public InputStream requestStream(PornRequest request) {
        try {
            Response response = client.newCall(configurateRequest(request)).execute();
            if (response.isSuccessful()) {
                return response.body().byteStream();
            }
            response.close();
        } catch (Exception e) {
            log.error("Error execute stream: ", e);
        }
        return InputStream.nullInputStream();
    }


    @Override
    public long checkContentLength(@NonNull PornRequest request) {
        try (Response response = client.newCall(configurateRequest(request)).execute()) {
            if (response.isSuccessful()) {
                return Long.parseLong(Objects.requireNonNull(response.header("Content-Length")));
            } else {
                log.warn("Non success check response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error execute checking: ", e);
        }

        return 0;
    }

    private @NotNull Request configurateRequest(@NotNull PornRequest request) {
        Request.Builder builder = new Request.Builder();
        builder.url(request.path().toString());

        Optional<HeadersConfig> optionalHttpConfig = httpConfig.headersConfig();
        if (optionalHttpConfig.isPresent()) {
            HeadersConfig headersConfig = optionalHttpConfig.get();
            Optional<Map<String, String>> optHeaders = headersConfig.headerSet();
            putHeaders(builder, optHeaders.orElse(request.headers()));
        } else {
            putHeaders(builder, request.headers());
        }

        if (request.method().equals("POST")) {
            throw new UnsupportedOperationException("POST method is not available in request provider!");
        } else if (request.method().equals("HEAD")) {
            builder.head();
        } else {
            builder.get();
        }

        return builder.build();
    }

    private void putHeaders(Request.Builder builder, @NonNull Map<String, String> headers) {
        for (String header : headers.keySet()) {
            builder.header(header, headers.get(header));
        }
    }

    @Override
    public void setProxy(Proxy proxy) {
        OkHttpClient.Builder builder = client.newBuilder();
        builder.proxy(Objects.requireNonNullElse(proxy, Proxy.NO_PROXY));
        client = builder.build();
    }

    @Contract("_ -> new")
    private @NonNull OkHttpClient configureHttpClient(@NonNull HttpConfig config) {
        return new OkHttpClient.Builder()
                .connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .retryOnConnectionFailure(config.retryPolicy() == RetryPolicy.ON_FAILED || config.retryPolicy() == RetryPolicy.ALWAYS)
                .cookieJar(new CookieJar() {
                    private final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

                    @Override
                    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public @NonNull List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        return cookieStore.getOrDefault(url.host(), new ArrayList<>());
                    }
                })
                .build();
    }

    @Override
    public void close() {
        try (ExecutorService service = client.dispatcher().executorService()) {
            service.shutdownNow();
            client.connectionPool().evictAll();
        } catch (Exception e) {
            log.error("OkHttp client shutdown error: ", e);
        }
    }
}