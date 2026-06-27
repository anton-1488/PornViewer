package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.services.files.PVFileManager;
import org.jetbrains.annotations.NotNull;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.exceptions.KeyerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class PVSecurityManager {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(PVFileManager.PORN_VIEWER);
    private static final Logger log = LoggerFactory.getLogger(PVSecurityManager.class);

    private PVSecurityManager() {
    }

    static {
        createPasswordIfNotExist();
    }

    public static void createPasswordIfNotExist() {
        try {
            String alias = PVFileManager.PORN_VIEWER;
            byte[] retrievedPassword = null;
            try {
                retrievedPassword = KEYCHAIN.getRawPassword(alias);
            } catch (KeyerException e) {
                log.error("Error to get password. Trying setup a new password. ", e);
            }

            if (retrievedPassword == null) {
                byte[] password = new byte[32];
                CipherEngineUtils.createRandomPassword(password);
                KEYCHAIN.setPassword(alias, password);
                log.info("New password generated and saved to keychain");
            } else {
                Arrays.fill(retrievedPassword, (byte) 0);
            }
        } catch (Exception e) {
            throw new KeyerException("Keychain error", e);
        }
    }

    public static byte @NotNull [] getPassword() {
        byte[] password = KEYCHAIN.getRawPassword(PVFileManager.PORN_VIEWER);
        if (password == null) {
            throw new NoSuchElementException("Master password not found in Keychain!");
        }
        return password;
    }
}