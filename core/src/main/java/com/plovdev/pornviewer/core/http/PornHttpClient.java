package com.plovdev.pornviewer.core.http;

import com.plovdev.pornviewer.core.http.events.OutputProcessor;
import com.plovdev.pornviewer.core.http.requests.HttpClientRequestProvider;
import com.plovdev.pornviewer.core.http.requests.OkHttpRequestProvider;
import com.plovdev.pornviewer.core.http.requests.PornRequestProvider;
import com.plovdev.pornviewer.database.UserSettingsManager;
import com.plovdev.pornviewer.pvvasupport.PVVASupportManager;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class PornHttpClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PornHttpClient.class);
    private static final HttpConfig DEFAULT_HTTP_CONFIG = new HttpConfig("OK_HTTP", new HeadersConfig(false, List.of(PornRequest.getDefaultHeaders())), RetryPolicy.ON_FAILED, 0, 0, 0, 3, 1000);
    private static final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private PornRequestProvider requestProvider;

    public PornHttpClient() {
        PVVAHost host = PVVASupportManager.loadPvvaById(UserSettingsManager.getUserSettings().adapter());
        if (host != null) {
            requestProvider = createProvider(host.optHttpConfig().orElse(DEFAULT_HTTP_CONFIG));
        } else {
            requestProvider = new OkHttpRequestProvider(DEFAULT_HTTP_CONFIG);
        }
    }

    @Contract(pure = true)
    public PornHttpClient(@NonNull HttpConfig config) {
        requestProvider = createProvider(config);
    }

    private @Nullable PornRequestProvider createProvider(@NonNull HttpConfig config) {
        return switch (config.httpClient().orElse("OK_HTTP")) {
            case "JAVA_HTTP_CLIENT" -> new HttpClientRequestProvider(config);
            case "APACHE_HTTP_CLEINT" -> null; // TODO: Add Apache http request provider
            default -> new OkHttpRequestProvider(config);
        };
    }

    public @NonNull List<Future<String>> executeAsync(List<PornRequest> requests) {
        Objects.requireNonNull(requests);
        List<Future<String>> futures = new ArrayList<>();
        for (PornRequest request : requests) {
            futures.add(asyncExecutor.submit(() -> executeString(request)));
        }
        return futures;
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public void setRequestProvider(PornRequestProvider requestProvider) {
        Objects.requireNonNull(requestProvider);
        this.requestProvider = requestProvider;
    }

    public String executeString(PornRequest request) {
        return requestProvider.executeGet(request);
    }

    public byte[] executeBytes(PornRequest request) {
        return requestProvider.executeRaw(request);
    }

    public void download(@NonNull PornRequest request, OutputProcessor processor) {
        log.info("Start loading file: {}", request.path().getPath());
        long videoSize = getContentSize(request);
        //TODO
    }

    public void setProxy(Proxy proxy) {
        requestProvider.setProxy(proxy);
    }

    public long getContentSize(PornRequest request) {
        return requestProvider.checkContentLength(request);
    }

    @Override
    public void close() {
        asyncExecutor.close();
    }
}