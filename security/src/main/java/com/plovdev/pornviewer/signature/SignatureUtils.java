package com.plovdev.pornviewer.signature;

import com.plovdev.pornviewer.exceptions.SignatureVerifyingException;

import java.security.Signature;

public final class SignatureUtils {
    public static final String ALGORITHM = "Ed25519";

    private SignatureUtils() {
    }

    public static boolean verifyData(byte[] publicKey, byte[] data, byte[] signBytes) {
        if (publicKey == null || publicKey.length != 32) {
            throw new IllegalArgumentException("Public key must be 32 bytes");
        }
        if (data == null || signBytes == null) {
            return false;
        }

        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(KeysUtils.getPublicKeyFromRaw(publicKey));
            signature.update(data);

            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new SignatureVerifyingException(e);
        }
    }
}