package com.plovdev.pornviewer.commons.events;

import org.plovdev.eda.ChannelEvent;
import org.plovdev.eda.EventManager;

public final class GlobalEventManager {
    private static final EventManager EVENT_MANAGER = new EventManager();

    private GlobalEventManager() {
        throw new UnsupportedOperationException();
    }

    public static void broadcastEvent(ChannelEvent event) {
        EVENT_MANAGER.broadcast(event);
    }

    public static void registerListener(Object listener) {
        EVENT_MANAGER.register(listener);
    }
}