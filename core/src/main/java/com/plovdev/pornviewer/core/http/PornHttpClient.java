package com.plovdev.pornviewer.core.http;

import com.plovdev.pornviewer.core.http.events.OutputProcessor;
import com.plovdev.pornviewer.core.http.requests.HttpClientRequestProvider;
import com.plovdev.pornviewer.core.http.requests.OkHttpRequestProvider;
import com.plovdev.pornviewer.core.http.requests.PornRequestProvider;
import com.plovdev.pornviewer.core.http.requests.RequestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class PornHttpClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PornHttpClient.class);
    private static final PornHttpClient pornHttpClient = new PornHttpClient();
    private static final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private PornRequestProvider requestProvider;

    public static PornHttpClient getInstance() {
        return pornHttpClient;
    }

    private PornHttpClient() {
        requestProvider = new OkHttpRequestProvider();
    }

    public PornHttpClient(PornRequestProvider requestProvider) {
        this.requestProvider = requestProvider;
    }

    public PornHttpClient(RequestProvider provider) {
        if (provider == RequestProvider.JAVA_HTTP_CLIENT) {
            requestProvider = new HttpClientRequestProvider();
        } else {
            requestProvider = new OkHttpRequestProvider();
        }
    }

    public List<Future<String>> executeAsync(List<PornRequest> requests) {
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

    public void download(PornRequest request, OutputProcessor processor) {
        log.info("Start loading file: {}", request.path().getPath());
        long videoSize = getContentSize(request);
        //TODO
    }

    public long getContentSize(PornRequest request) {
        return requestProvider.checkContentLength(request);
    }

    @Override
    public void close() {
        asyncExecutor.close();
    }
}