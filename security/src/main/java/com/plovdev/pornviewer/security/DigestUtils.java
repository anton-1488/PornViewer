package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.services.crypto.HexUtils;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.List;
import java.util.NoSuchElementException;

public final class DigestUtils {
    public static final String MD2 = "MD2";
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA_224 = "SHA-224";
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_384 = "SHA-384";
    public static final String SHA_512 = "SHA-512";
    public static final String SHA3_224 = "SHA3-224";
    public static final String SHA3_256 = "SHA3-256";
    public static final String SHA3_384 = "SHA3-384";
    public static final String SHA3_512 = "SHA3-512";
    public static final String SHA512_224 = "SHA-512/224";
    public static final String SHA512_256 = "SHA-512/256";
    public static final String SHAKE128_256 = "SHAKE128-256";
    public static final String SHAKE256_512 = "SHAKE256-512";

    private DigestUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String md2(String plain) {
        return processAlgorithm(MD2, plain);
    }

    public static String md2(byte[] plain) {
        return processAlgorithm(MD2, plain);
    }

    public static String md5(String plain) {
        return processAlgorithm(MD5, plain);
    }

    public static String md5(byte[] plain) {
        return processAlgorithm(MD5, plain);
    }

    public static String sha1(String plain) {
        return processAlgorithm(SHA1, plain);
    }

    public static String sha1(byte[] plain) {
        return processAlgorithm(SHA1, plain);
    }

    public static String sha224(String plain) {
        return processAlgorithm(SHA_224, plain);
    }

    public static String sha224(byte[] plain) {
        return processAlgorithm(SHA_224, plain);
    }

    public static String sha256(String plain) {
        return processAlgorithm(SHA_256, plain);
    }

    public static String sha256(byte[] plain) {
        return processAlgorithm(SHA_256, plain);
    }

    public static String sha384(String plain) {
        return processAlgorithm(SHA_384, plain);
    }

    public static String sha384(byte[] plain) {
        return processAlgorithm(SHA_384, plain);
    }

    public static String sha512(String plain) {
        return processAlgorithm(SHA_512, plain);
    }

    public static String sha512(byte[] plain) {
        return processAlgorithm(SHA_512, plain);
    }

    public static String sha3_224(String plain) {
        return processAlgorithm(SHA3_224, plain);
    }

    public static String sha3_224(byte[] plain) {
        return processAlgorithm(SHA3_224, plain);
    }

    public static String sha3_256(String plain) {
        return processAlgorithm(SHA3_256, plain);
    }

    public static String sha3_256(byte[] plain) {
        return processAlgorithm(SHA3_256, plain);
    }

    public static String sha3_384(String plain) {
        return processAlgorithm(SHA3_384, plain);
    }

    public static String sha3_384(byte[] plain) {
        return processAlgorithm(SHA3_384, plain);
    }

    public static String sha3_512(String plain) {
        return processAlgorithm(SHA3_512, plain);
    }

    public static String sha3_512(byte[] plain) {
        return processAlgorithm(SHA3_512, plain);
    }

    public static String sha512_224(String plain) {
        return processAlgorithm(SHA512_224, plain);
    }

    public static String sha512_224(byte[] plain) {
        return processAlgorithm(SHA512_224, plain);
    }

    public static String sha512_256(String plain) {
        return processAlgorithm(SHA512_256, plain);
    }

    public static String sha512_256(byte[] plain) {
        return processAlgorithm(SHA512_256, plain);
    }

    public static String shake128_256(String plain) {
        return processAlgorithm(SHAKE128_256, plain);
    }

    public static String shake128_256(byte[] plain) {
        return processAlgorithm(SHAKE128_256, plain);
    }

    public static String shake256_512(String plain) {
        return processAlgorithm(SHAKE256_512, plain);
    }

    public static String shake256_512(byte[] plain) {
        return processAlgorithm(SHAKE256_512, plain);
    }

    public static String processAlgorithm(@NonNull String algorithm, @NonNull String plain) {
        return processAlgorithm(algorithm, plain.getBytes(StandardCharsets.UTF_8));
    }

    public static @NonNull List<String> getSupportedAlgorithms() {
        return Security.getAlgorithms("MessageDigest").stream().toList();
    }

    public static boolean isDigestsEquals(String dig1, String dig2) {
        if (dig1 == null || dig2 == null) return false;
        return isDigestsEquals(dig1.getBytes(StandardCharsets.UTF_8), dig2.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isDigestsEquals(byte[] dig1, byte[] dig2) {
        return MessageDigest.isEqual(dig1, dig2);
    }

    @Contract(pure = true)
    public static boolean isAlgorithmSupported(@NonNull String algorithm) {
        try {
            MessageDigest.getInstance(algorithm);
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public static String processAlgorithm(@NonNull String algorithm, byte @NonNull [] plainBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(plainBytes);
            return HexUtils.ofHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchElementException("Algorithm " + algorithm + " not found.", e);
        }
    }
}