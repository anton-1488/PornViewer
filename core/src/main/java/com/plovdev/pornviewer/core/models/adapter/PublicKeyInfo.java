package com.plovdev.pornviewer.core.models.adapter;

import java.util.Objects;
import java.util.UUID;

public record PublicKeyInfo(UUID developerId, String username, byte[] publicKey, KeyStatus keyStatus) {
    public enum KeyStatus {
        ACTIVE, PAUSED, UNAVAILABLE
    }

    public PublicKeyInfo {
        Objects.requireNonNull(developerId);
        Objects.requireNonNull(username);
        Objects.requireNonNull(publicKey);
        if (publicKey.length != 32) {
            throw new IllegalArgumentException("Public key length must be a 32 bytes.");
        }
        Objects.requireNonNull(keyStatus);
    }
}