package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.utils.NumberUtils;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class CipherEngineUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int ITERATIONS = 300000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static final int CHACHA20_NONCE_LENGTH = 12;
    public static final int BASE_NONCE_LENGTH = 8;
    public static final int COUNTER_NONCE_LENGTH = 4;

    public static @NonNull SecretKeySpec createSecretKeySpecFromPassword(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] key = factory.generateSecret(spec).getEncoded();
        SecretKeySpec result = new SecretKeySpec(key, "ChaCha20");
        Arrays.fill(key, (byte) 0);
        Arrays.fill(password, ' ');
        return result;
    }

    public static @NonNull IvParameterSpec createParameterSpecFromBaseNonce(long counter, byte @NonNull [] baseNonce) {
        if (baseNonce.length != BASE_NONCE_LENGTH) {
            throw new IllegalArgumentException("Illegal base nonce length! Make sure that nonce length is 8 byte!");
        }

        byte[] fullNonce = new byte[CHACHA20_NONCE_LENGTH];
        byte[] counterNonce = NumberUtils.intToBytes((int) counter);
        System.arraycopy(baseNonce, 0, fullNonce, 0, baseNonce.length);
        System.arraycopy(counterNonce, 0, fullNonce, BASE_NONCE_LENGTH, counterNonce.length);

        return createParameterSpecFromNonce(fullNonce);
    }

    @Contract("_ -> new")
    public static @NonNull IvParameterSpec createParameterSpecFromNonce(byte @NonNull [] nonce) {
        if (nonce.length != CHACHA20_NONCE_LENGTH) {
            throw new IllegalArgumentException("Illegal nonce length! Make sure that nonce length is 12 byte!");
        }
        return new IvParameterSpec(nonce);
    }

    public static void createRandomPassword(byte[] toWrite) {
        SECURE_RANDOM.nextBytes(toWrite);
    }
}