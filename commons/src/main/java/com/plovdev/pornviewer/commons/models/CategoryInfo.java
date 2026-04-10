package com.plovdev.pornviewer.commons.models;

import org.jetbrains.annotations.NotNull;

public record CategoryInfo(String name, String url) {
    @Override
    @NotNull
    public String toString() {
        return "CategoryInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}