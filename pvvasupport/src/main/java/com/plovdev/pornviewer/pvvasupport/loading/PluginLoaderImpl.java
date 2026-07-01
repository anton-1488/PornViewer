package com.plovdev.pornviewer.pvvasupport.loading;

import com.plovdev.pornviewer.pvvasupport.exceptions.PluginLoadingException;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginNotVerifiedException;
import com.plovdev.pornviewer.pvvasupport.verifiers.HashPluginVerifier;
import com.plovdev.pornviewer.pvvasupport.verifiers.PluginVerifier;
import com.plovdev.pornviewer.pvvasupport.verifiers.SignaturePluginVerifier;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.read.ByteArrayPVVAReader;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class PluginLoaderImpl implements PluginLoader {
    /**
     * {@inheritDoc}
     */
    @Override
    public PVVAHost loadFromServer(String pluginId, URI pluginUri) {
        try {
            PluginDownloader downloader = new PluginDownloader();
            byte[] pluginData = downloader.downloadPlugin(pluginId, pluginUri);
            try (PVVAReader reader = new ByteArrayPVVAReader(pluginData)) {
                PVVAHost host = reader.readVideoAdapter();
                PluginJson pluginJson = host.pluginJson();
                PVVAHeader header = host.header();
                PluginVerifier verifier = new SignaturePluginVerifier(pluginJson.developerId());

                if (verifier.checkPluginIdNeed(pluginData)) {
                    return host;
                } else {
                    throw new PluginNotVerifiedException("Plugin " + header.getPluginId() + " is not verified when load from server.");
                }
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
            PVVAHeader header = host.header();
            PluginVerifier verifier = new HashPluginVerifier(host.getSystemPluginId(), header.isHasSign());

            if (verifier.checkPluginIdNeed(reader.getReadData().array())) {
                return host;
            } else {
                throw new PluginNotVerifiedException("Plugin " + header.getPluginId() + " is not verified when load from disk.");
            }
        } catch (IOException e) {
            throw new PluginLoadingException("Error to load pvva plugin", e);
        }
    }
}