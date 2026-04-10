package com.plovdev.pornviewer.core.http.requests;

import com.plovdev.pornviewer.core.http.PornRequest;

import java.io.InputStream;

/**
 * Интерфейс для реализации провайдера сетевого клиента.
 */
public interface PornRequestProvider {
    String executeGet(PornRequest request);
    byte[] executeRaw(PornRequest request);
    InputStream requestStream(PornRequest request);

    long checkContentLength(PornRequest request);
}