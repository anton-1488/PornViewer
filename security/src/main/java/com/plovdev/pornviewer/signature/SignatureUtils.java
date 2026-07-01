package com.plovdev.pornviewer.signature;

import com.plovdev.pornviewer.exceptions.SignatureVerifyingException;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

public final class SignatureUtils {
    public static final String ALGORITHM = "Ed25519";

    private SignatureUtils() {
    }

    public static boolean verifyData(byte[] publicKey, byte[] data, byte[] signBytes) {
        try {
            Signature signature = prepareSignature(publicKey, signBytes);
            signature.update(data);
            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new SignatureVerifyingException(e);
        }
    }

    public static boolean verifyData(byte[] publicKey, ByteBuffer data, byte[] signBytes) {
        try {
            Signature signature = prepareSignature(publicKey, signBytes);
            signature.update(data);
            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new SignatureVerifyingException(e);
        }
    }

    private static @NonNull Signature prepareSignature(byte[] publicKey, byte[] signBytes) throws NoSuchAlgorithmException, InvalidKeyException {
        if (publicKey == null || publicKey.length != 32) {
            throw new IllegalArgumentException("Public key must be 32 bytes");
        }
        if (signBytes == null || signBytes.length != 64) {
            throw new IllegalArgumentException("Signature must be 64 bytes.");
        }

        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(KeysUtils.getPublicKeyFromRaw(publicKey));

        return signature;
    }
}