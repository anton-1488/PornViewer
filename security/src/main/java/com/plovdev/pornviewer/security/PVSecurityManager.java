package com.plovdev.pornviewer.security;

import com.github.javakeyring.Keyring;
import com.plovdev.pornviewer.utils.files.PVFileManager;

public class PVSecurityManager {
    private PVSecurityManager() {

    }

    public static char[] getPassword() {
        try (Keyring keyring = Keyring.create()) {
            return keyring.getPassword(PVFileManager.PORN_VIEWER_SIGN, PVFileManager.PORN_VIEWER_SIGN).toCharArray();
        } catch (Exception e) {
            throw new NullPointerException("Getted password is null!");
        }
    }
}