package com.plovdev.pornviewer.pvvasupport.loading;

import com.plovdev.pornviewer.core.models.adapter.PluginsListItem;
import com.plovdev.pornviewer.pvvasupport.PluginsUtils;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginLoadingException;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginNotVerifiedException;
import com.plovdev.pornviewer.pvvasupport.loading.validator.PluginValidator;
import com.plovdev.pornviewer.pvvasupport.verifiers.HashPluginVerifier;
import com.plovdev.pornviewer.pvvasupport.verifiers.PluginVerifier;
import com.plovdev.pornviewer.pvvasupport.verifiers.SignaturePluginVerifier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.read.ByteArrayPVVAReader;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;

import java.io.IOException;
import java.nio.file.Path;

public class PluginLoaderImpl implements PluginLoader {
    /**
     * {@inheritDoc}
     */
    @Override
    public PVVAHost loadFromServer(PluginsListItem pluginItem) {
        try {
            String pluginId = pluginItem.systemPluginId();
            byte[] pluginData = PluginDownloader.downloadPlugin(pluginId, pluginItem.downloadUrl());

            try (PVVAReader reader = new ByteArrayPVVAReader(pluginData)) {
                PVVAHost host = reader.readVideoAdapter();
                PluginJson pluginJson = host.pluginJson();
                checkLoadedHost(new SignaturePluginVerifier(pluginJson.developerId()), host, pluginData);

                PluginsUtils.saveDownloadedPlugin(pluginId, pluginData);
                return host;
            }
        } catch (Exception e) {
            throw new PluginLoadingException("Error to load pvva plugin from server", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PVVAHost loadFromDisk(String pluginId, Path path) {
        try (PVVAReader reader = new DefaultPVVAReader(path)) {
            PVVAHost host = reader.readVideoAdapter();
            PluginVerifier verifier = new HashPluginVerifier(host.getSystemPluginId());
            return checkLoadedHost(verifier, host, reader.getReadData().array());
        } catch (IOException e) {
            throw new PluginLoadingException("Error to load pvva plugin", e);
        }
    }

    @Contract("_, _, _ -> param2")
    private PVVAHost checkLoadedHost(@NonNull PluginVerifier verifier, PVVAHost host, byte[] pluginData) {
        if (verifier.checkPluginIfNeed(pluginData)) {
            PluginValidator.validatePlugin(host);
            return host;
        } else {
            throw new PluginNotVerifiedException("Plugin " + host.getSystemPluginId() + " is not verified.");
        }
    }
}