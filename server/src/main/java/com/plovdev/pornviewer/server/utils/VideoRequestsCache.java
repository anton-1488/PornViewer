package com.plovdev.pornviewer.server.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.plovdev.pornviewer.server.models.VideoRequestSet;

import java.io.File;
import java.time.Duration;

public final class VideoRequestsCache {
    private VideoRequestsCache() {
    }

    private static final Cache<File, VideoRequestSet> CACHED_VIDEOS = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(100)
            .build();

    public static VideoRequestSet getRequestSet(File requestedFile) {
        return CACHED_VIDEOS.get(requestedFile, RequetUtils::loadEncryptedVideo);
    }
}