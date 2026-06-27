package com.plovdev.pornviewer.http.providers;

import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.exceptions.RequestProviderException;

import java.io.InputStream;
import java.net.Proxy;

/**
 * Интерфейс для реализации провайдера сетевого клиента.
 */
public interface PornRequestProvider extends AutoCloseable {
    String executeGet(PornRequest request) throws RequestProviderException;

    byte[] executeRaw(PornRequest request) throws RequestProviderException;

    InputStream requestStream(PornRequest request) throws RequestProviderException;

    long checkContentLength(PornRequest request) throws RequestProviderException;

    void setProxy(Proxy proxy);

    @Override
    void close();
}