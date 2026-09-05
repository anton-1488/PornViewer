package com.plovdev.pornviewer.server.handlers;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class PluginsHandler extends AbstractServerHandler {
    @Override
    protected void onRequest(HttpExchange exchange, URI uri, HttpMethod method, Map<String, Object> params) throws Exception {

    }

    @Override
    protected List<HttpMethod> getSupportedMethods() {
        return List.of(HttpMethod.GET, HttpMethod.POST);
    }
}