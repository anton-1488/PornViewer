package com.plovdev.pornviewer.server;

import com.plovdev.pornviewer.events.listeners.ServerEventListener;
import com.plovdev.pornviewer.events.listeners.ServerEventListenerAdapter;
import com.plovdev.pornviewer.server.handlers.DeepLinkHandler;
import com.plovdev.pornviewer.server.handlers.SafeHttpHandler;
import com.plovdev.pornviewer.server.handlers.UtilsHandler;
import com.plovdev.pornviewer.server.handlers.VideoExportHandler;
import com.plovdev.pornviewer.utility.files.ServerPaths;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SafeHttpServer {
    private final Logger logger = LoggerFactory.getLogger(SafeHttpServer.class);

    public static final int PORT = 3535;
    private boolean isRunning;
    private HttpServer server;
    private volatile static SafeHttpServer INSTANCE = null;
    private ServerEventListener listener = new ServerEventListenerAdapter() {};

    public static SafeHttpServer getInstance() {
        if (INSTANCE == null) {
            synchronized (SafeHttpServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SafeHttpServer();
                }
            }
        }
        return INSTANCE;
    }

    private SafeHttpServer() {}

    public void restartServer() {
        stopServer();
        startServer();
    }

    public void startServer() {
        if (isRunning) {
            logger.info("Server already running");
            return;
        }
        ServerPaths.updateToken(UUID.randomUUID().toString());

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/video", new SafeHttpHandler());
            server.createContext("/info", new UtilsHandler());
            server.createContext("/deeplink", new DeepLinkHandler());
            server.createContext("/export", new VideoExportHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            Thread.sleep(250);
            isRunning = true;
            logger.info("Server started on port {}", PORT);
            listener.onServerStarted();
        } catch (BindException e) {
            listener.onAdressAlreadyInUse(new InetSocketAddress(PORT));
            logger.error("Adress creating error: ", e);
        } catch (Exception e) {
            listener.onError(e);
            logger.error("Server start error: ", e);
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            isRunning = false;
            logger.info("Server stopped");
            listener.onServerStopped();
        }
    }

    public ServerEventListener getListener() {
        return listener;
    }

    public void setListener(ServerEventListener listener) {
        Objects.requireNonNull(listener);
        this.listener = listener;
    }

    public boolean isRunning() {
        return isRunning;
    }
}