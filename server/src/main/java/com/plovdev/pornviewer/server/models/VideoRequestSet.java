package com.plovdev.pornviewer.server.models;

import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.security.CryptoEngine;

import java.util.Objects;

public record VideoRequestSet(EncryptedVideo encryptedVideo, CryptoEngine cryptoEngine) {
    public VideoRequestSet {
        Objects.requireNonNull(encryptedVideo);
        Objects.requireNonNull(cryptoEngine);
    }
}