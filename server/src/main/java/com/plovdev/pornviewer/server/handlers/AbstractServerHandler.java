package com.plovdev.pornviewer.server.handlers;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.core.http.HttpUtils;
import com.plovdev.pornviewer.core.utils.Globals;
import com.plovdev.pornviewer.server.exceptions.PornViewerServerException;
import com.plovdev.pornviewer.server.utils.ServerSendsUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractServerHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractServerHandler.class);

    @Override
    public final void handle(@NonNull HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
            Headers headers = exchange.getRequestHeaders();

            if (!getSupportedMethods().contains(method)) {
                log.warn("Method {} not allowed here", method);
                if (method == HttpMethod.HEAD) {
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                } else {
                    ServerSendsUtils.send405(exchange);
                    exchange.close();
                }
                return;
            }

            String authorizationHeader = "Authorization";
            if (headers.containsKey(authorizationHeader)) {
                String authorization = headers.getFirst(authorizationHeader);
                if (authorization == null || authorization.isBlank()) {
                    log.warn("Authorization is empty.");
                    ServerSendsUtils.send401(exchange);
                } else {
                    UUID authToken = UUID.fromString(authorization);
                    if (Globals.APP_ACCESS_TOKEN.equals(authToken)) {
                        log.debug("Authorization success, handling request");
                        String body;
                        if (method == HttpMethod.POST) {
                            try (InputStream is = exchange.getRequestBody()) {
                                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            }
                        } else {
                            body = uri.toString();
                        }
                        onRequest(exchange, uri, method, HttpUtils.parseResponseBody(method, body));
                    } else {
                        ServerSendsUtils.send403(exchange);
                    }
                }
            } else {
                ServerSendsUtils.send401(exchange);
            }
        } catch (Exception e) {
            log.error("Error process request: ", e);
            try {
                ServerSendsUtils.send500(exchange);
            } catch (IOException ex) {
                throw new PornViewerServerException(ex);
            }
        }
    }

    /**
     * Метод для обработки запроса.
     *
     * @param uri    запрашиваемый путь.
     * @param params параметры запрос. Query string в GET, или json body в POST.
     */
    protected abstract void onRequest(HttpExchange exchange, URI uri, HttpMethod method, Map<String, Object> params) throws Exception;

    /**
     * Возвращает поддерживаемый реализацией http-метод.
     *
     * @return поддерживаемый http-метод.
     */
    protected abstract List<HttpMethod> getSupportedMethods();
}