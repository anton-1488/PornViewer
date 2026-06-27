package com.plovdev.pornviewer.core.models.porn;

import com.plovdev.pornviewer.core.models.video.VideoRating;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public record ShortVideoInfo(String id, String title, URI url, URI picture, Duration duration, int views,
                             VideoRating rating) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShortVideoInfo that = (ShortVideoInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}