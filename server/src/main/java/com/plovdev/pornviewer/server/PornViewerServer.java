package com.plovdev.pornviewer.server;

import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.ServerChannelEvent;
import com.plovdev.pornviewer.core.utils.Globals;
import com.plovdev.pornviewer.server.exceptions.PornViewerServerException;
import com.plovdev.pornviewer.server.handlers.AppInfoHandler;
import com.plovdev.pornviewer.services.files.EnvReader;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервер для внутренних задач PornViewer, таких как стриминг зашифрованных видео, экспорт, получение ифонрмации и тд.
 *
 * @author Anton
 */
public class PornViewerServer {
    private static final Logger log = LoggerFactory.getLogger(PornViewerServer.class);
    private static final PornViewerServer INSTANCE = new PornViewerServer();

    private final AtomicBoolean isServerStarted = new AtomicBoolean(false);
    private final HttpServer server;

    private final String host;
    private final int port;
    private final int backlog;
    private final int stopDelay;

    public static PornViewerServer getInstance() {
        return INSTANCE;
    }

    private PornViewerServer() {
        try {
            EnvReader reader = new EnvReader("/configs/server.properties");
            this.host = reader.getEnv("host");
            this.port = Integer.parseInt(reader.getEnv("port"));
            this.backlog = Integer.parseInt(reader.getEnv("backlog"));
            this.stopDelay = Integer.parseInt(reader.getEnv("stop-delay"));

            server = HttpServer.create(new InetSocketAddress(host, port), backlog);
            server.setExecutor(Globals.VIRTUAL_EXECUTOR);

            //TODO: create handlers
//            server.createContext("/video", null);
//            server.createContext("/export", null);
//            server.createContext("/plugins", null);
            server.createContext("/app-info", new AppInfoHandler());
        } catch (Exception e) {
            throw new PornViewerServerException("Can't create server: ", e);
        }
    }

    public String getHost() {
        return host;
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

    public synchronized void restartServer() {
        if (isServerStarted.get()) {
            stopServer();
        }
        startServer();
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