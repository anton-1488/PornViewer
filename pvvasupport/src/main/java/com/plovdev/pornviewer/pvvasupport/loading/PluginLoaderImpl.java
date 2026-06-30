package com.plovdev.pornviewer.pvvasupport.loading;

import com.plovdev.pornviewer.pvvasupport.exceptions.AdapterLoadingException;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginNotVerifiedException;
import com.plovdev.pornviewer.pvvasupport.verifiers.HashPluginVerifier;
import com.plovdev.pornviewer.pvvasupport.verifiers.PluginVerifier;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
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
    public PVVAHost loadFromServer(URI pluginUri) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PVVAHost loadFromDisk(Path path) {
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
            throw new AdapterLoadingException("Error to load pvva plugin", e);
        }
    }
}