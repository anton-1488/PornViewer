package com.plovdev.pornviewer;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import com.plovdev.pornviewer.databases.SecureDB;
import com.plovdev.pornviewer.encryptionsupport.CipherEngineUtils;
import com.plovdev.pornviewer.events.listeners.ServerEventListenerAdapter;
import com.plovdev.pornviewer.gui.MainMenu;
import com.plovdev.pornviewer.server.SafeHttpServer;
import com.plovdev.pornviewer.utility.LauncherHelper;
import com.plovdev.pornviewer.utility.deeplink.DeepLinker;
import com.plovdev.pornviewer.utility.files.FileUtils;
import javafx.application.Application;
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

    static {
        System.setProperty("sun.net.httpserver.maxReqTime", "600");
        System.setProperty("sun.net.httpserver.maxRspTime", "600");
    }

    public static void main(String[] args) {
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(ImageIO.read(Objects.requireNonNull(Launcher.class.getResourceAsStream("/com/plovdev/pornviewer/pv-logo.png"))));
            }
        } catch (Exception e) {
            log.error("Error setup image icon: ", e);
        }
        try {
            initPassword();
            SecureDB.initDB();
            startServer(args);

            Path downloadsPath = FileUtils.getPvDownloadsPath();
            if (!Files.exists(downloadsPath)) {
                Files.createDirectories(downloadsPath);
            }
            Path systemPath = Path.of(FileUtils.getPvSystemPath());
            if (!Files.exists(systemPath)) {
                Files.createDirectories(systemPath);
            }
            Path dbFile = Path.of(FileUtils.getPvDbPath());
            if (!Files.exists(dbFile)) {
                Files.createFile(dbFile);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

    private static URI getDeepLink(String[] args) {
        for (String lnk : args) {
            if (lnk.startsWith("pv://") || lnk.startsWith("pornviewer://")) {
                return URI.create(lnk);
            }
        }
        return null;
    }

    private static void initPassword() {
        try (Keyring keyring = Keyring.create()) {
            String service = FileUtils.PORN_VIEWER_SIGN;
            String account = FileUtils.PORN_VIEWER_SIGN;
            String retrievedPassword = null;

            try {
                retrievedPassword = keyring.getPassword(service, account);
            } catch (PasswordAccessException e) {
                log.info("Password not found in keychain, creating new one...");
            }

            if (retrievedPassword == null) {
                byte[] password = new byte[32];
                CipherEngineUtils.createRandomPassword(password);
                String newPassword = new String(password);
                keyring.setPassword(service, account, newPassword);
                log.info("New password generated and saved to keychain");
                System.gc();
            }
        } catch (Exception e) {
            log.error("Failed to access keychain", e);
            throw new RuntimeException("Keychain error", e);
        }
    }
}