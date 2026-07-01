package com.plovdev.pornviewer.core.http;

import com.plovdev.pornviewer.core.exceptions.PornViewerException;
import com.plovdev.pornviewer.core.exceptions.RequestProviderException;
import com.plovdev.pornviewer.core.exceptions.UnsuccessResponseException;
import com.plovdev.pornviewer.core.utils.Globals;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InternalHttpClient {
    private static final HttpClient client;

    static {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .executor(Globals.VIRTUAL_EXECUTOR)
                .build();
        Globals.addShutdownHook(() -> {
            client.shutdownNow();
            client.close();
        });
    }

    private InternalHttpClient() {
        throw new UnsupportedOperationException();
    }

    public static String execute(PornRequest request) {
        try {
            HttpResponse<String> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();
            if (code == 200) {
                return body;
            } else {
                throw new UnsuccessResponseException(String.format("Internal server return unsuccess response(code: %d): %s", code, body));
            }
        } catch (IOException e) {
            throw new RequestProviderException("IO error to communicate with server", e);
        } catch (IllegalArgumentException | InterruptedException e) {
            throw new PornViewerException("Can't execute to internal server", e);
        }
    }

    public static InputStream executeStream(PornRequest request) {
        try {
            HttpResponse<InputStream> response = client.send(cofigureRequest(request), HttpResponse.BodyHandlers.ofInputStream());
            int code = response.statusCode();
            if (code == 200) {
                return response.body();
            } else {
                throw new UnsuccessResponseException(String.format("Internal server return unsuccess response for raw request(code: %d)", code));
            }
        } catch (IOException e) {
            throw new RequestProviderException("IO error to communicate with server", e);
        } catch (IllegalArgumentException | InterruptedException e) {
            throw new PornViewerException("Can't execute to internal server", e);
        }
    }

    private static HttpRequest cofigureRequest(@NotNull PornRequest request) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(request.path());

        Map<String, String> headers = PornRequest.getInternalHeaders();
        for (String header : headers.keySet()) {
            requestBuilder.header(header, headers.get(header));
        }

        if (request.method() == HttpMethod.POST) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(request.body()));
        } else if (request.method() == HttpMethod.HEAD) {
            requestBuilder.HEAD();
        } else {
            requestBuilder.GET();
        }

        return requestBuilder.build();
    }
}