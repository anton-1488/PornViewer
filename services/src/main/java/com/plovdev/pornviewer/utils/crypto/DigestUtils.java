package com.plovdev.pornviewer.utils.crypto;

import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class DigestUtils {
    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    public static String md5(String plain) {
        return processAlgorithm("MD5", plain);
    }

    public static String sha256(String plain) {
        return processAlgorithm("SHA-256", plain);
    }

    public static String processAlgorithm(String algorithm, @NonNull String plain) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(plain.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        return HEX.formatHex(bytes);
    }
}