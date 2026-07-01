package com.plovdev.pornviewer.pvvasupport.api;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.core.http.HttpUtils;
import com.plovdev.pornviewer.core.http.InternalHttpClient;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.models.adapter.PluginsList;
import com.plovdev.pornviewer.services.files.EnvReader;
import com.plovdev.pornviewer.services.json.JSONSerializer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PluginsCatalogRequestor {
    private static final String HTTP_PATHS_CONFIG = "/http-paths.properties";

    private int minAppVersion;
    private int maxAppVersion;
    private int page;
    private String query;

    public PluginsCatalogRequestor(int minAppVersion, int maxAppVersion, int page, String query) {
        this.minAppVersion = minAppVersion;
        this.maxAppVersion = maxAppVersion;
        this.page = page;
        this.query = query;
    }

    public PluginsCatalogRequestor() {
    }

    public int getMinAppVersion() {
        return minAppVersion;
    }

    public void setMinAppVersion(int minAppVersion) {
        this.minAppVersion = minAppVersion;
    }

    public int getMaxAppVersion() {
        return maxAppVersion;
    }

    public void setMaxAppVersion(int maxAppVersion) {
        this.maxAppVersion = maxAppVersion;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public PluginsList requestCatalog() {
        EnvReader reader = new EnvReader(HTTP_PATHS_CONFIG);
        String baseUrl = reader.getEnv("base.url");
        String endpoint = reader.getEnv("get-plugins-catalog.url");

        Map<String, Object> queryMap = new HashMap<>();
        if (minAppVersion > 0) {
            queryMap.put("minAppVersion", minAppVersion);
        }
        if (maxAppVersion > 0) {
            queryMap.put("maxAppVersion", maxAppVersion);
        }
        if (page > 0) {
            queryMap.put("page", page);
        }
        if (query != null && !query.isBlank()) {
            queryMap.put("query", query);
        }

        String queryString = HttpUtils.formatRequestBody(HttpMethod.GET, queryMap);
        String fullUrl = baseUrl + endpoint;
        if (!queryString.isEmpty()) {
            fullUrl += "?" + queryString;
        }

        URI queryUri = URI.create(fullUrl);
        return JSONSerializer.deserialize(InternalHttpClient.execute(PornRequest.get(queryUri)), PluginsList.class);
    }
}