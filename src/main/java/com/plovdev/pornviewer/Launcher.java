package com.plovdev.pornviewer;

import com.plovdev.pornviewer.databases.SecureDB;
import com.plovdev.pornviewer.encryptionsupport.CipherEngineUtils;
import com.plovdev.pornviewer.events.listeners.ServerEventListenerAdapter;
import com.plovdev.pornviewer.gui.MainMenu;
import com.plovdev.pornviewer.server.SafeHttpServer;
import com.plovdev.pornviewer.utility.LauncherHelper;
import com.plovdev.pornviewer.utility.deeplink.DeepLinker;
import com.plovdev.pornviewer.utility.files.FileUtils;
import javafx.application.Application;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);
    private static final LauncherHelper launcherHelper = LauncherHelper.getInstance();
    private static final Keychain KEYCHAIN = Keychain.getKeychain(FileUtils.PORN_VIEWER_SIGN);

    static {
        System.setProperty("sun.net.httpserver.maxReqTime", "600");
        System.setProperty("sun.net.httpserver.maxRspTime", "600");
    }

    static void main(String[] args) {
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(ImageIO.read(Objects.requireNonNull(Launcher.class.getResourceAsStream("/com/plovdev/pornviewer/pv-logo.png"))));
            }
        } catch (Throwable e) {
            log.debug("Error setup image icon: ", e);
        }

        if (args.length > 1) {
            String arg = args[0];
            String val = args[1];
            if (arg.equals("--app-path")) {
                if (Files.exists(Path.of(val))) {
                    log.info("Using another path: {}", val);
                    System.setProperty("pv.home", val + "/.PornViewer/");
                }
            }
        }

        try {
            FileUtils fileUtils = new FileUtils();

            Path downloadsPath = fileUtils.getPvDownloadsPath();
            if (!Files.exists(downloadsPath)) {
                Files.createDirectories(downloadsPath);
            }
            Path systemPath = Path.of(fileUtils.getPvSystemPath());
            if (!Files.exists(systemPath)) {
                Files.createDirectories(systemPath);
            }
            Path dbFile = Path.of(fileUtils.getPvDbPath());
            if (!Files.exists(dbFile)) {
                Files.createFile(dbFile);
            }
        } catch (Exception e) {
            log.error("Error to create paths structure: ", e);
        }

        CipherEngineUtils.initPassword();
        SecureDB.initDB();
        startServer(args);

        log.info("Launching pv...");
        DeepLinker.init(launcherHelper);
        Application.launch(MainMenu.class, args);
    }

    private static void startServer(String[] args) {
        URI deeplink = getDeepLink(args);

        SafeHttpServer server = SafeHttpServer.getInstance();
        server.setListener(new ServerEventListenerAdapter() {
            @Override
            public void onAdressAlreadyInUse(InetSocketAddress address) {
                if (!launcherHelper.checkPrimaryApp()) {
                    log.warn("App is not primary, exiting...");
                    if (deeplink != null) {
                        launcherHelper.notifyDeepLink(deeplink);
                    }
                    System.exit(0);
                }
            }

            @Override
            public void onServerStarted() {
                if (deeplink != null) {
                    launcherHelper.notifyDeepLink(deeplink);
                }
            }
        });
        server.startServer();
    }

    private static @Nullable URI getDeepLink(String @NonNull [] args) {
        for (String lnk : args) {
            if (lnk.startsWith("pv://") || lnk.startsWith("pornviewer://")) {
                return URI.create(lnk);
            }
        }
        return null;
    }
}