package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.services.crypto.HexUtils;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
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
            return HexUtils.ofHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " algorithm not found", e);
        }
    }
}