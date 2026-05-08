package com.plovdev.pornviewer.commons.models;

import java.net.URI;
import java.util.Objects;

public record CategoryInfo(String name, URI url) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CategoryInfo that = (CategoryInfo) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}