package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.security.exceptions.PornViewerSecurityException;
import com.plovdev.pornviewer.services.NumberUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoEngine {
    public static final String ALGORITHM = "ChaCha20-Poly1305/None/NoPadding";

    private final int mode;
    private final byte[] baseNonce;
    private final SecretKeySpec keySpec;

    public CryptoEngine(int mode, byte[] password, byte[] baseNonce) {
        try {
            this.mode = mode;
            this.baseNonce = baseNonce;
            keySpec = CryptoUtils.createSecretKeySpecFromPassword(password, baseNonce);
        } catch (Exception e) {
            throw new PornViewerSecurityException(e);
        }
    }

    public int getMode() {
        return mode;
    }

    public byte[] getBaseNonce() {
        return baseNonce;
    }

    public synchronized byte[] processChunk(long counter, byte[] block) {
        try {
            IvParameterSpec parameterSpec = CryptoUtils.createParameterSpecFromBaseNonce(counter, baseNonce);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, parameterSpec);
            cipher.updateAAD(NumberUtils.longToBytes(counter));

            return cipher.doFinal(block);
        } catch (Exception e) {
            throw new PornViewerSecurityException(e);
        }
    }

    public synchronized byte[] processData(byte[] data, byte[] nonce, byte[] id) {
        try {
            IvParameterSpec parameterSpec = CryptoUtils.createParameterSpecFromNonce(nonce);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, parameterSpec);
            cipher.updateAAD(id);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new PornViewerSecurityException(e);
        }
    }
}