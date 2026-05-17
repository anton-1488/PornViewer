package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.utils.files.PVFileManager;
import org.jetbrains.annotations.NotNull;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PVSecurityManager {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(PVFileManager.PORN_VIEWER);
    private static final Logger log = LoggerFactory.getLogger(PVSecurityManager.class);

    private PVSecurityManager() {
    }

    public static void createPasswordIfNotExist() {
        try {
            String alias = PVFileManager.PORN_VIEWER;
            char[] retrievedPassword = null;
            try {
                retrievedPassword = KEYCHAIN.getPassword(alias);
            } catch (KeyerException e) {
                log.error("Error to get password. Trying setup a new password. ", e);
            }

            if (retrievedPassword == null) {
                byte[] password = new byte[32];
                CipherEngineUtils.createRandomPassword(password);
                KEYCHAIN.setPassword(alias, NativeUtils.bytesToCharsUTF_8(password));
                log.info("New password generated and saved to keychain");
            } else {
                Arrays.fill(retrievedPassword, '\u0000');
            }
        } catch (Exception e) {
            throw new KeyerException("Keychain error", e);
        }
    }

    public static char @NotNull [] getPassword() {
        char[] password = KEYCHAIN.getPassword(PVFileManager.PORN_VIEWER);
        if (password == null) {
            throw new IllegalStateException("Master password not found in Keychain!");
        }
        return password;
    }
}