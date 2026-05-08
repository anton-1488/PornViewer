package com.plovdev.pornviewer.commons.models;

import java.net.URI;
import java.util.Objects;

public record ModelInfo(String name, URI url, URI avatar, String country, int videoCount) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelInfo modelInfo = (ModelInfo) o;
        return Objects.equals(url, modelInfo.url) && Objects.equals(name, modelInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }
}