package com.plovdev.pornviewer.core.events;

import org.plovdev.eda.ChannelEvent;

public class PluginDownloadingChannel extends ChannelEvent {
    public static final String CHANNEL = "pvva.download";

    private final String pluginId;
    private final DownloadingType type;
    private final long bytes;
    private final Exception error;

    public PluginDownloadingChannel(String pluginId, long bytes, DownloadingType type) {
        super(CHANNEL);
        this.pluginId = pluginId;
        this.type = type;
        this.bytes = bytes;
        error = null;
    }

    public PluginDownloadingChannel(String pluginId, Exception e, DownloadingType type) {
        super(CHANNEL);
        this.pluginId = pluginId;
        this.type = type;
        this.error = e;
        bytes = -1L;
    }

    public DownloadingType getType() {
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
        return String.format("PLUGIN DOWNLOADING EVENT: [channel: %s, bytes: %d, type: %s]", getChannel(), bytes, type.name());
    }
}