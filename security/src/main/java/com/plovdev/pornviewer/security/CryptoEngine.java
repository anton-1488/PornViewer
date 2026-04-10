package com.plovdev.pornviewer.security;

import com.plovdev.pornviewer.utils.NumberUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoEngine {
    public static final String ALGORITHM = "ChaCha20-Poly1305/None/NoPadding";
    private int mode;
    private byte[] baseNonce;

    private SecretKeySpec keySpec;

    public CryptoEngine(int mode, char[] password, byte[] baseNonce) {
        try {
            this.mode = mode;
            this.baseNonce = baseNonce;
            keySpec = CipherEngineUtils.createSecretKeySpecFromPassword(password, baseNonce);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getMode() {
        return mode;
    }

    public synchronized void setMode(int mode) {
        this.mode = mode;
    }

    public byte[] getBaseNonce() {
        return baseNonce;
    }

    public synchronized void setBaseNonce(char[] password, byte[] baseNonce) {
        try {
            // set nonce and update keySpec:
            this.baseNonce = baseNonce;
            keySpec = CipherEngineUtils.createSecretKeySpecFromPassword(password, baseNonce);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized byte[] processChunk(long counter, byte[] block) {
        try {
            IvParameterSpec parameterSpec = CipherEngineUtils.createParameterSpecFromBaseNonce(counter, baseNonce);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, parameterSpec);
            cipher.updateAAD(NumberUtils.longToBytes(counter));

            return cipher.doFinal(block);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized byte[] processData(byte[] data, byte[] nonce, byte[] id) {
        try {
            IvParameterSpec parameterSpec = CipherEngineUtils.createParameterSpecFromNonce(nonce);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, parameterSpec);
            cipher.updateAAD(id);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}