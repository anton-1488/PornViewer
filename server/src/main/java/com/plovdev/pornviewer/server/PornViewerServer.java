package com.plovdev.pornviewer.server;

import com.plovdev.pornviewer.commons.events.GlobalEventManager;
import com.plovdev.pornviewer.commons.events.ServerChannelEvent;
import com.plovdev.pornviewer.commons.utils.Globals;
import com.plovdev.pornviewer.exceptions.PornViewerServerException;
import com.plovdev.pornviewer.utils.files.EnvReader;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class PornViewerServer {
    private static final Logger log = LoggerFactory.getLogger(PornViewerServer.class);
    private static final PornViewerServer INSTANCE = new PornViewerServer();

    private final AtomicBoolean isServerStarted = new AtomicBoolean(false);
    private final HttpServer server;

    private final int port;
    private final int backlog;
    private final int stopDelay;

    public static PornViewerServer getInstance() {
        return INSTANCE;
    }

    private PornViewerServer() {
        try {
            EnvReader reader = new EnvReader("/configs/server.properties");
            this.port = Integer.parseInt(reader.getEnv("port"));
            this.backlog = Integer.parseInt(reader.getEnv("backlog"));
            this.stopDelay = Integer.parseInt(reader.getEnv("stop-delay"));

            server = HttpServer.create(new InetSocketAddress(port), backlog);
            server.setExecutor(Globals.VIRTUAL_EXECUTOR);

            //TODO: create handlers
            server.createContext("/video", null);
            server.createContext("/app-info", null);
            server.createContext("/deeplink", null);
        } catch (Exception e) {
            throw new PornViewerServerException("Can't create server: ", e);
        }
    }

    public int getPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }

    public int getStopDelay() {
        return stopDelay;
    }

    public boolean getIsServerStarted() {
        return isServerStarted.get();
    }

    public void startServer() {
        if (isServerStarted.compareAndSet(false, true)) {
            server.start();
            log.info("PornViewer local server started...");
            GlobalEventManager.broadcastEvent(new ServerChannelEvent(ServerChannelEvent.ServerEventType.SERVER_STARTED));
        } else {
            throw new IllegalStateException("Server already started");
        }
    }

    public void stopServer() {
        if (isServerStarted.compareAndSet(true, false)) {
            server.stop(stopDelay);
            log.info("PornViewer local server stopped...");
            GlobalEventManager.broadcastEvent(new ServerChannelEvent(ServerChannelEvent.ServerEventType.SERVER_STOPPED));
        } else {
            throw new IllegalStateException("Server already stopped");
        }
    }
}