package com.plovdev.pornviewer.services.http;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public final class UriBuilder {
    private final StringBuilder uriBuilder;

    public UriBuilder(String baseUrl) {
        uriBuilder = new StringBuilder(baseUrl);
    }

    public void appendUriPart(String part) {
        uriBuilder.append(part);
    }

    @Contract(" -> new")
    public @NonNull URI build() {
        return URI.create(uriBuilder.toString());
    }

    @Override
    public String toString() {
        return uriBuilder.toString();
    }
}