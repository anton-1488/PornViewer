package com.plovdev.pornviewer.pvvasupport.verifiers;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.tables.VerifiedHashes;
import com.plovdev.pornviewer.security.DigestUtils;
import org.jspecify.annotations.NonNull;

public class HashPluginVerifier implements PluginVerifier {
    private final String hashId;

    public HashPluginVerifier(String hashId) {
        this.hashId = hashId;
    }

    @Override
    public boolean verifyPlugin(byte @NonNull [] pluginData) {
        if (pluginData.length <= 64) {
            throw new IllegalArgumentException("Plugin data too short: " + pluginData.length);
        }

        VerifiedHash verifiedHash = VerifiedHashes.getVerifiedHash(hashId);
        byte[] pluginHash = DigestUtils.sha256(pluginData);
        return DigestUtils.isDigestsEquals(verifiedHash.hash(), pluginHash);
    }
}