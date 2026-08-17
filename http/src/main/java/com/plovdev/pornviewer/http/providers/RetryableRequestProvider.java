package com.plovdev.pornviewer.http.providers;

import com.plovdev.pornviewer.core.exceptions.RequestProviderException;
import com.plovdev.pornviewer.core.exceptions.UnsuccessResponseException;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.utils.Globals;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.Proxy;

public class RetryableRequestProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(RetryableRequestProvider.class);

    private final PornRequestProvider requestProvider;
    private HttpConfig httpConfig;

    public RetryableRequestProvider(PornRequestProvider requestProvider, HttpConfig httpConfig) {
        this.requestProvider = requestProvider;
        this.httpConfig = httpConfig;
    }

    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    public void setHttpConfig(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    @Override
    public String executeGet(PornRequest request) throws RequestProviderException {
        return executeRetryable(() -> requestProvider.executeGet(request));
    }

    @Override
    public byte[] executeRaw(PornRequest request) throws RequestProviderException {
        return executeRetryable(() -> requestProvider.executeRaw(request));
    }

    @Override
    public InputStream requestStream(PornRequest request) throws RequestProviderException {
        return executeRetryable(() -> requestProvider.requestStream(request));
    }

    @Override
    public long checkContentLength(PornRequest request) throws RequestProviderException {
        return executeRetryable(() -> requestProvider.checkContentLength(request));
    }

    private <T> T executeRetryable(@NonNull RequestExecutorSupplier<T> request) {
        RetryPolicy policy = httpConfig.retryPolicy();
        int retryCount = httpConfig.retryCount();

        if (policy == RetryPolicy.NO_RETRY || retryCount <= 0) {
            log.debug("No retry policy, creating single request.");
            return request.execute();
        }

        for (int i = 0; i < retryCount; i++) {
            try {
                return request.execute();
            } catch (RequestProviderException e) {
                log.warn("Retry {}/{} for request", i + 1, retryCount);
                Globals.sleep(httpConfig.retryDelay());

                if (policy == RetryPolicy.ON_NON_SUCCESS) {
                    log.debug("Retrying by non success response policy");
                    if (!(e instanceof UnsuccessResponseException ure)) {
                        throw e;
                    }
                } else if (policy == RetryPolicy.ON_FAILED) {
                    log.debug("Retrying by on_failed policy");
                }
            }
        }

        throw new RequestProviderException("Error to retry this request");
    }

    @FunctionalInterface
    private interface RequestExecutorSupplier<T> {
        T execute() throws RequestProviderException;
    }

    @Override
    public void setProxy(Proxy proxy) {
        requestProvider.setProxy(proxy);
    }

    @Override
    public void close() {
        requestProvider.close();
    }
}