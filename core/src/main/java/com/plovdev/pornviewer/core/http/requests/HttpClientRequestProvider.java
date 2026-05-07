package com.plovdev.pornviewer.core.http.requests;

import com.plovdev.pornviewer.core.http.PornRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class HttpClientRequestProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(HttpClientRequestProvider.class);
    private final HttpClient client;

    public HttpClientRequestProvider(HttpConfig config) {
        CookieHandler.setDefault(new CookieManager());
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    @Override
    public String executeGet(PornRequest request) {
        try {
            HttpResponse<String> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.error("Request error: ", e);
        }
        return "";
    }

    @Override
    public byte[] executeRaw(PornRequest request) {
        try {
            HttpResponse<byte[]> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.error("Error to request porn: ", e);
        }
        return new byte[0];
    }

    @Override
    public InputStream requestStream(PornRequest request) {
        try {
            HttpResponse<InputStream> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.error("Error to request stream: ", e);
        }

        return InputStream.nullInputStream();
    }

    @Override
    public void setProxy(Proxy proxy) {

    }

    @Override
    public long checkContentLength(@NonNull PornRequest request) {
        if (!request.method().equals("HEAD")) {
            request = PornRequest.head(request.path());
        }

        try {
            HttpResponse<Void> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 200) {
                return response.headers().firstValue("Content-Length").map(Long::parseLong).orElse(0L);
            }
        } catch (Exception e) {
            log.error("Content length checking error: ", e);
        }
        return 0;
    }

    private HttpRequest cofigureRequest(@NotNull PornRequest request) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(request.path());

        Map<String, String> headers = request.headers();
        for (String header : headers.keySet()) {
            requestBuilder.header(header, headers.get(header));
        }

        if (request.method().equals("POST")) {
            throw new UnsupportedOperationException("POST method is not available in request provider!");
        } else if (request.method().equals("HEAD")) {
            requestBuilder.HEAD();
        } else {
            requestBuilder.GET();
        }

        return requestBuilder.build();
    }
}