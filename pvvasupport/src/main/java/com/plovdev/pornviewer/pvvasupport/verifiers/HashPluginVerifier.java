package com.plovdev.pornviewer.pvvasupport.verifiers;

import com.plovdev.pornviewer.core.models.app.VerifiedHash;
import com.plovdev.pornviewer.database.tables.VerifiedHashes;
import com.plovdev.pornviewer.security.DigestUtils;
import org.jspecify.annotations.NonNull;

public class HashPluginVerifier implements PluginVerifier {
    private final String hashId;
    private final boolean hasSignature;

    public HashPluginVerifier(String hashId, boolean hasSignature) {
        this.hashId = hashId;
        this.hasSignature = hasSignature;
    }

    @Override
    public boolean verifyPlugin(byte @NonNull [] pluginData) {
        if (pluginData.length <= 24) { // plugin's header size
            throw new IllegalArgumentException("Plugin data too short: " + pluginData.length);
        }

        if (pluginData.length > 64 && hasSignature) {
            int pluginDataLength = pluginData.length - 64;
            byte[] rawPluginData = new byte[pluginDataLength];
            System.arraycopy(pluginData, 0, rawPluginData, 0, pluginDataLength);
            pluginData = rawPluginData;
        }

        VerifiedHash verifiedHash = VerifiedHashes.getVerifiedHash(hashId);
        byte[] pluginHash = DigestUtils.sha256(pluginData);

        return DigestUtils.isDigestsEquals(verifiedHash.hash(), pluginHash);
    }
}