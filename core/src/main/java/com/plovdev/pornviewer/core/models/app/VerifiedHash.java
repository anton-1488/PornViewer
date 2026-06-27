package com.plovdev.pornviewer.core.models.app;

import com.plovdev.pornviewer.core.exceptions.NotFoundException;
import org.jspecify.annotations.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Objects;

public record VerifiedHash(@NonNull String hashId, @NonNull HashedFileType fileType, @NonNull Path filePath,
                           byte @NonNull [] hash) {
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public enum HashedFileType {
        PLUGIN, SYSTEM, OTHER
    }

    public VerifiedHash {
        Objects.requireNonNull(hashId);
        Objects.requireNonNull(fileType);
        Objects.requireNonNull(filePath);
        Objects.requireNonNull(hash);

        if (Files.notExists(filePath)) {
            throw new NotFoundException("File not found by received file path: " + filePath);
        }

        if (hash.length != 32) {
            throw new IllegalArgumentException("Hash length not equals SHA-256 hash length. " + hash.length);
        }
    }

    public VerifiedHash(@NonNull String hashId, @NonNull String fileType, @NonNull String filePath, @NonNull String hash) {
        this(hashId, HashedFileType.valueOf(fileType.trim().toUpperCase()), Path.of(filePath), HEX_FORMAT.parseHex(hash));
    }

    public String hashString() {
        return HEX_FORMAT.formatHex(hash());
    }

    @Override
    public @NonNull String toString() {
        return "VerifiedHash{" +
                "hashId='" + hashId + '\'' +
                ", fileType=" + fileType +
                ", filePath=" + filePath +
                ", hash=" + hashString() +
                '}';
    }
}