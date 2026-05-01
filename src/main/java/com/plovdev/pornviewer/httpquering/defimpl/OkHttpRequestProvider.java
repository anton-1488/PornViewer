package com.plovdev.pornviewer.httpquering.defimpl;

import com.plovdev.pornviewer.httpquering.PornRequest;
import com.plovdev.pornviewer.httpquering.PornRequestProvider;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OkHttpRequestProvider implements PornRequestProvider {
    private static final Logger log = LoggerFactory.getLogger(OkHttpRequestProvider.class);
    private final OkHttpClient client;

    public OkHttpRequestProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .cookieJar(new CookieJar() {
                    private final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public @NotNull List<Cookie> loadForRequest(@NotNull HttpUrl url) {
                        return cookieStore.getOrDefault(url.host(), new ArrayList<>());
                    }
                })
                .build();
    }

    @Override
    public String executeGet(PornRequest request) {
        try (Response response = client.newCall(configurateRequest(request)).execute()) {
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).string();
            } else {
                log.warn("Non success get response: {}", response);
            }
        } catch (UnknownHostException e) {
            log.debug("Cann't to connect: ", e);
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
        } catch (UnknownHostException e) {
            log.debug("Cann't to connect to raw: ", e);
        } catch (Exception e) {
            log.error("Error execute raw: ", e);
        }
        return new byte[0];
    }

    @Override
    public String executePost(PornRequest request) {
        return executeGet(request);
    }

    @Override
    public InputStream requestStream(PornRequest request) {
        try {
            Response response = client.newCall(configurateRequest(request)).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().byteStream();
            }
            response.close();
        } catch (UnknownHostException e) {
            log.debug("Cann't to connect to stream: ", e);
        } catch (Exception e) {
            log.error("Error execute stream: ", e);
        }
        return InputStream.nullInputStream();
    }


    @Override
    public long checkContentLength(PornRequest request) {
        if (!request.method().equals("HEAD")) {
            request = PornRequest.head(request.url());
        }

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
        builder.url(request.url());

        Map<String, String> headers = request.headers();
        for (String header : headers.keySet()) {
            builder.header(header, headers.get(header));
        }

        if (request.method().equals("POST")) {
            builder.post(RequestBody.create(request.body(), MediaType.parse("application/x-www-form-urlencoded")));
        } else if (request.method().equals("HEAD")) {
            builder.head();
        } else {
            builder.get();
        }

        return builder.build();
    }
}
