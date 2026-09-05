package com.plovdev.pornviewer.server.models;

public record CalculatedVideoParams(String mime, long start, long end, long metadataSize, long videoStart, long endVideoLength, long realStart, long realEnd, long contentLength, long realContentSize) {
}