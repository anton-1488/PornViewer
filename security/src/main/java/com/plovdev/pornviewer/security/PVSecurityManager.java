package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.utils.files.PVFileManager;
import org.jetbrains.annotations.NotNull;
import org.plovdev.keyer.Keychain;

public class PVSecurityManager {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(PVFileManager.PORN_VIEWER);

    private PVSecurityManager() {
    }

    public static char @NotNull [] getPassword() {
        char[] password = KEYCHAIN.getPassword(PVFileManager.PORN_VIEWER);
        if (password == null) {
            throw new IllegalStateException("Master password not found in Keychain!");
        }
        return password;
    }
}