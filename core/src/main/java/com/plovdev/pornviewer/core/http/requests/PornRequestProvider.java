package com.plovdev.pornviewer.core.http.requests;

import com.plovdev.pornviewer.core.http.PornRequest;

import java.io.InputStream;
import java.net.Proxy;

/**
 * Интерфейс для реализации провайдера сетевого клиента.
 */
public interface PornRequestProvider extends AutoCloseable {
    String executeGet(PornRequest request);
    byte[] executeRaw(PornRequest request);
    InputStream requestStream(PornRequest request);
    void setProxy(Proxy proxy);
    long checkContentLength(PornRequest request);

    @Override
    void close();
}