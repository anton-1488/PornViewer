package com.plovdev.pornviewer;

import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.http.providers.OkHttpRequestProvider;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpClientType;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main(String[] args) {
        OkHttpRequestProvider provider = new OkHttpRequestProvider(new HttpConfig(HttpClientType.OK_HTTP_CLIENT, new HeadersConfig(false, List.of(PornRequest.getDefaultHeaders())), RetryPolicy.ON_FAILED, 1000, 1000, 1000, 1000, 3));
        String html = provider.executeGet(PornRequest.get("https://trahkino.me"));
        System.out.println(html);
    }
}