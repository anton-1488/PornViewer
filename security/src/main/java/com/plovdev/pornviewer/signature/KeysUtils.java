package com.plovdev.pornviewer.signature;

import com.plovdev.pornviewer.exceptions.PornViewerSecurityException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.internal.asn1.edec.EdECObjectIdentifiers;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public final class KeysUtils {
    private KeysUtils() {
    }

    public static PublicKey getPublicKeyFromRaw(byte[] publicKey) {
        try {
            SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), publicKey);
            byte[] encodedKey = subjectPublicKeyInfo.getEncoded();

            KeyFactory keyFactory = KeyFactory.getInstance(SignatureUtils.ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new PornViewerSecurityException(e);
        }
    }
}