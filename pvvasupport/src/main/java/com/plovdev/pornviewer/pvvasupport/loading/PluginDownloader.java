package com.plovdev.pornviewer.pvvasupport.loading;

import com.plovdev.pornviewer.core.events.DownloadingType;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.PluginDownloadingChannel;
import com.plovdev.pornviewer.core.http.InternalHttpClient;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.pvvasupport.exceptions.PluginLoadingException;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

public final class PluginDownloader {
    private static final int PLUGIN_READ_CHUNK = 1024;

    private PluginDownloader() {
    }

    public static byte @NonNull [] downloadPlugin(String pluginId, URI pluginUri) {
        try (InputStream pluginStream = InternalHttpClient.executeStream(PornRequest.get(pluginUri));
             ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            byte[] chunkBuffer = new byte[PLUGIN_READ_CHUNK];
            long totalReaded = 0;
            int readed;

            while ((readed = pluginStream.readNBytes(chunkBuffer, 0, PLUGIN_READ_CHUNK)) > 0) {
                stream.write(chunkBuffer);
                GlobalEventManager.broadcastEvent(new PluginDownloadingChannel(pluginId, totalReaded, DownloadingType.PROCESS));
                totalReaded += readed;
            }

            return stream.toByteArray();
        } catch (Exception e) {
            throw new PluginLoadingException(e);
        }
    }
}