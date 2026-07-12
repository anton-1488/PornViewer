package com.plovdev.pornviewer.server.handlers;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.server.utils.ServerSendsUtils;
import com.plovdev.pornviewer.services.files.ConfigReader;
import com.plovdev.pornviewer.services.json.JSONSerializer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AppInfoHandler extends AbstractServerHandler {
    private final AtomicReference<String> cachedAppInfo = new AtomicReference<>(null);

    @Override
    protected void onRequest(HttpExchange exchange, URI uri, Map<String, Object> params) throws IOException {
        String appInfoStr = cachedAppInfo.updateAndGet(currentValue -> Objects.requireNonNullElseGet(currentValue, () -> JSONSerializer.serialize(ConfigReader.loadAppInfo())));

        ServerSendsUtils.sendJson(exchange, appInfoStr);
    }

    @Override
    protected List<HttpMethod> getSupportedMethods() {
        return List.of(HttpMethod.GET);
    }
}