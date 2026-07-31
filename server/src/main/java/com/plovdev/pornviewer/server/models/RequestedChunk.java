package com.plovdev.pornviewer.server.models;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record RequestedChunk(long start, long end) {
    @Contract("_ -> new")
    public static @NonNull RequestedChunk fullChunk(long end) {
        return new RequestedChunk(0, end);
    }

    public static @NonNull RequestedChunk parseChunk(String chunkStr, long maxEnd) {
        if (chunkStr == null || !chunkStr.startsWith("bytes=")) {
            throw new IllegalArgumentException("Invalid range header format. Expected 'bytes=...'");
        }

        String rangeValue = chunkStr.substring(6); // Remove "bytes=" prefix
        int dashIndex = rangeValue.lastIndexOf("-");
        if (dashIndex == -1) {
            throw new IllegalArgumentException("Invalid range format. Missing hyphen separator.");
        }

        try {
            String startStr = rangeValue.substring(0, dashIndex);
            String endStr = rangeValue.substring(dashIndex + 1);

            long start = startStr.isEmpty() ? 0 : Long.parseLong(startStr);
            long end = endStr.isEmpty() ? maxEnd - 1 : Long.parseLong(endStr);

            // Validate range bounds
            if (start < 0) {
                throw new IllegalArgumentException("Start position cannot be negative: " + start);
            }
            if (end >= maxEnd) {
                end = maxEnd - 1;
            }
            if (start > end) {
                throw new IllegalArgumentException(String.format("Invalid range: start (%d) > end (%d)", start, end));
            }

            return new RequestedChunk(start, end);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values in range: " + rangeValue, e);
        }
    }
}