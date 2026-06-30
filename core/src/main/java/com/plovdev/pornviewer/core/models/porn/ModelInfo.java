package com.plovdev.pornviewer.core.models.porn;

import java.net.URI;
import java.util.Objects;

public record ModelInfo(String name, URI url, URI avatar, Country country, int videoCount) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelInfo modelInfo = (ModelInfo) o;
        return Objects.equals(name, modelInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}