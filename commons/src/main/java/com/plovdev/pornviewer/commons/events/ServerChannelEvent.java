package com.plovdev.pornviewer.commons.events;

import org.plovdev.eda.ChannelEvent;

public class ServerChannelEvent extends ChannelEvent {
    public static final String CHANNEL = "server.state";

    public enum ServerEventType {
        SERVER_STARTED, SERVER_STOPPED
    }

    private final ServerEventType type;

    public ServerChannelEvent(ServerEventType type) {
        super(CHANNEL);
        this.type = type;
    }

    public ServerEventType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("SERVER EVENT: [channel: %s, type: %s]", getChannel(), type.name());
    }
}