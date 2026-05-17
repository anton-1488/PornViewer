package com.plovdev.pornviewer.commons.events;

import org.plovdev.eda.ChannelEvent;

public class VideoDownloadingChannel extends ChannelEvent {
    public static final String CHANNEL = "video.download";

    public enum DownloadedType {
        START, PROCESS, ERROR, END
    }

    private final DownloadedType type;
    private final long bytes;
    private final Exception error;

    public VideoDownloadingChannel(long bytes, DownloadedType type) {
        super(CHANNEL);
        this.type = type;
        this.bytes = bytes;
        error = null;
    }

    public VideoDownloadingChannel(Exception e, DownloadedType type) {
        super(CHANNEL);
        this.type = type;
        this.error = e;
        bytes = -1L;
    }

    public DownloadedType getType() {
        return type;
    }

    public Exception getError() {
        return error;
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return String.format("DOWNLOADING EVENT: [channel: %s, bytes: %d, type: %s]", getChannel(), bytes, type.name());
    }
}