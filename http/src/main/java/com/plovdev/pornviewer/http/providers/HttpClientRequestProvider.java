package com.plovdev.pornviewer.http.providers;

import com.plovdev.pornviewer.core.exceptions.NoInternetException;
import com.plovdev.pornviewer.core.exceptions.RequestProviderException;
import com.plovdev.pornviewer.core.exceptions.UnsuccessResponseException;
import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.core.http.PornRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.plovdev.pornviewer.core.exceptions.NoInternetException.ERR_MESSAGE;

public class HttpClientRequestProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(HttpClientRequestProvider.class);
    private final HttpClient.Builder HTTP_BUILDER = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_2)
            .cookieHandler(new CookieManager());

    private final AtomicReference<HttpClient> client = new AtomicReference<>();
    private final HttpConfig config;

    public HttpClientRequestProvider(@NonNull HttpConfig config) {
        client.set(HTTP_BUILDER.connectTimeout(Duration.ofMillis(config.connectTimeout())).build());
        this.config = config;
    }

    @Override
    public String executeGet(PornRequest request) {
        return processResponse(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public byte[] executeRaw(PornRequest request) {
        return processResponse(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    @Override
    public InputStream requestStream(PornRequest request) {
        return processResponse(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    @Override
    public long checkContentLength(@NonNull PornRequest request) {
        try {
            HttpResponse<Void> response = client.get().send(cofigureRequest(request), HttpResponse.BodyHandlers.discarding());
            int code = response.statusCode();

            if (isSuccessful(code)) {
                return response.headers().firstValue("Content-Length").map(Long::parseLong).orElse(0L);
            } else {
                throw new UnsuccessResponseException("Non success 'get' response", code);
            }
        } catch (UnknownHostException e) {
            throw new NoInternetException(ERR_MESSAGE, e);
        } catch (IOException | InterruptedException e) {
            throw new RequestProviderException(e);
        }
    }

    private HttpRequest cofigureRequest(@NotNull PornRequest request) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .timeout(Duration.ofMillis(config.readTimeout()))
                .uri(request.path());

        Map<String, String> headers = PornRequest.getDefaultHeaders();
        for (String header : headers.keySet()) {
            requestBuilder.header(header, headers.get(header));
        }

        if (request.method() == HttpMethod.POST) {
            throw new UnsupportedOperationException("POST method is not available in request provider!");
        } else if (request.method() == HttpMethod.HEAD) {
            requestBuilder.HEAD();
        } else {
            requestBuilder.GET();
        }

        return requestBuilder.build();
    }

    private <T> T processResponse(PornRequest request, HttpResponse.BodyHandler<T> handler) {
        try {
            HttpResponse<T> response = client.get().send(cofigureRequest(request), handler);
            int code = response.statusCode();
            T body = response.body();

            if (isSuccessful(code)) {
                return body;
            } else {
                throw new UnsuccessResponseException("Non success 'get' response", code);
            }
        } catch (UnknownHostException e) {
            throw new NoInternetException(ERR_MESSAGE, e);
        } catch (IOException | InterruptedException e) {
            throw new RequestProviderException(e);
        }
    }

    private boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    @Override
    public void setProxy(Proxy proxy) {
        HTTP_BUILDER.proxy(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return List.of(proxy == null ? Proxy.NO_PROXY : proxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                log.error("Error to connect to proxy. URI: {}, SocksAddress: {}. ERROR: ", uri, sa, ioe);
            }
        });
        client.set(HTTP_BUILDER.build());
    }

    @Override
    public void close() {
        HttpClient httpClient = client.getAndSet(null);
        httpClient.shutdownNow();
        httpClient.close();
    }
}