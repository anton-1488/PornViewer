package com.plovdev.pornviewer.utils.events;

import org.plovdev.eda.ChannelEvent;

public class VideoDownloadingChannel extends ChannelEvent<Long> {
    public enum DownloadedType {
        START, PROCESS, ERROR, END
    }

    private DownloadedType type;
    private Exception error;

    public VideoDownloadingChannel(String channel, Long eventData, DownloadedType type) {
        super(channel, eventData);
        this.type = type;
    }

    public VideoDownloadingChannel(String channel, Exception e, DownloadedType type) {
        super(channel, -1L);
        this.error = e;
    }

    public DownloadedType getType() {
        return type;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public void setType(DownloadedType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("DOWNLOADING EVENT: [channel: %s, bytes: %d, type: %s]", getChannel(), getEventData(), type.name());
    }
}