package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.security.keys.KeysEncoder;
import com.plovdev.pornviewer.security.keys.KeysEncoderImpl;
import com.plovdev.pornviewer.security.keys.KeysManager;
import com.plovdev.pornviewer.security.keys.KeysManagerImpl;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class PVSecurityManager {
    private static final Logger log = LoggerFactory.getLogger(PVSecurityManager.class);
    private static final KeysManager KEYS_MANAGER = new KeysManagerImpl();
    private static final KeysEncoder KEYS_ENCODER = new KeysEncoderImpl();

    private PVSecurityManager() {
    }

    static {
        KEYS_MANAGER.initKeysIfNotExist();
    }

    public static boolean verifyAppPin(String appPin) {
        log.info("Verifying app pin");
        if (appPin == null || appPin.isBlank()) {
            throw new IllegalArgumentException("App pin should be not empty.");
        }

        Optional<byte[]> userPinHash = KEYS_MANAGER.getUserPinHash();
        if (userPinHash.isPresent()) {
            byte[] pin = userPinHash.get();
            char[] appPinChars = appPin.toCharArray();          // will be clear in KeysEncoder.
            try {
                return KEYS_ENCODER.verify(appPinChars, pin);
            } finally {
                Arrays.fill(pin, (byte) 0);
            }
        } else {
            log.info("User not specificate pin, skipping.");
            return true; // No hash - skip verifying(user not specificate pin).
        }
    }

    public static byte[] getPassword(@NonNull RegisteredSecurityModule module) {
        return KEYS_MANAGER.getKeyForModule(module.getModuleId());
    }
}