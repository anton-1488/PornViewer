package com.plovdev.pornviewer.pvvasupport.verifiers;

import com.plovdev.pornviewer.core.http.InternalHttpClient;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.models.adapter.PublicKeyInfo;
import com.plovdev.pornviewer.services.files.ConfigReader;
import com.plovdev.pornviewer.services.json.JSONSerializer;
import com.plovdev.pornviewer.security.signature.SignatureUtils;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public class SignaturePluginVerifier implements PluginVerifier {
    private static final String HTTP_PATHS_CONFIG = "/http-paths.properties";
    private final String developerId;

    public SignaturePluginVerifier(String developerId) {
        this.developerId = developerId;
    }

    @Override
    public boolean verifyPlugin(byte @NonNull [] pluginData) {
        if (pluginData.length < 64) {
            throw new IllegalArgumentException("Plugin data too short: " + pluginData.length);
        }

        ConfigReader reader = new ConfigReader(HTTP_PATHS_CONFIG);
        String baseUrl = reader.getEnv("base.url");
        String endpoint = reader.getEnv("get-public-key.url") + "?developerId=" + developerId;

        URI queryUri = URI.create(baseUrl + endpoint);
        PublicKeyInfo keyInfo = JSONSerializer.deserialize(InternalHttpClient.execute(PornRequest.get(queryUri)), PublicKeyInfo.class);

        if (keyInfo.keyStatus() != PublicKeyInfo.KeyStatus.ACTIVE) {
            return false;
        }

        int pluginDataLength = pluginData.length - 64;
        byte[] rawPluginData = new byte[pluginDataLength];
        byte[] signatureBytes = new byte[64];
        System.arraycopy(pluginData, 0, rawPluginData, 0, pluginDataLength);
        System.arraycopy(pluginData, pluginDataLength, signatureBytes, 0, 64);

        return SignatureUtils.verifyData(keyInfo.publicKey(), rawPluginData, signatureBytes);
    }
}