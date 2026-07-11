package com.plovdev.pornviewer.security.keys;

import com.plovdev.pornviewer.security.CryptoUtils;
import com.plovdev.pornviewer.security.DigestUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.plovdev.pornviewer.security.CryptoUtils.*;

public class KeysEncoderImpl implements KeysEncoder {
    private static final Logger log = LoggerFactory.getLogger(KeysEncoderImpl.class);

    @Override
    public byte[] encode(char[] password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            CryptoUtils.createRandomPassword(salt);

            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            try {
                byte[] result = new byte[SALT_LENGTH + hash.length];
                System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
                System.arraycopy(hash, 0, result, SALT_LENGTH, hash.length);

                return result;
            } finally {
                Arrays.fill(hash, (byte) 0);
                Arrays.fill(salt, (byte) 0);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchElementException("Algorithm " + ALGORITHM + " not found.", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Key spec is invalid", e);
        } finally {
            Arrays.fill(password, '\u0000');
        }
    }

    @Override
    public boolean verify(char[] password, byte @NonNull [] encoded) {
        try {
            if (encoded.length != SALT_LENGTH + KEY_LENGTH_BYTES) {
                return false;
            }

            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(encoded, 0, salt, 0, SALT_LENGTH);
            byte[] storedHash = new byte[KEY_LENGTH_BYTES];
            System.arraycopy(encoded, SALT_LENGTH, storedHash, 0, storedHash.length);

            try {
                PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
                byte[] computedHash = factory.generateSecret(spec).getEncoded();
                return DigestUtils.isDigestsEquals(storedHash, computedHash);
            } finally {
                Arrays.fill(salt, (byte) 0);
                Arrays.fill(storedHash, (byte) 0);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error verify password: ", e);
            return false;
        } finally {
            Arrays.fill(password, '\u0000');
            Arrays.fill(encoded, (byte) 0);
        }
    }
}