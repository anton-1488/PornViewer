package com.plovdev.pornviewer;

import com.plovdev.pornviewer.core.events.DownloadingType;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.VideoDownloadingChannel;
import com.plovdev.pornviewer.core.exceptions.UnsuccessResponseException;
import com.plovdev.pornviewer.core.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.core.models.video.VideoQuality;
import com.plovdev.pornviewer.http.PornClient;
import com.plovdev.pornviewer.http.PornClientImpl;
import com.plovdev.pornviewer.core.models.video.DownloadedVideoInfo;
import org.jspecify.annotations.NonNull;
import org.plovdev.eda.reflect.Subscribe;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger(PornViewer.class);

    static void main() {
        GlobalEventManager.registerListener(new Listener());
        try (PVVAReader reader = new DefaultPVVAReader(Path.of("/Users/mac/.PornViewer/plugins/Porn365.pvva"));
             PornClient client = new PornClientImpl(reader.readVideoAdapter())) {

            FullVideoInfo info = client.requestVideoPage("49644");
            System.out.println(info);

            Map<VideoQuality, URI> qualityMap = info.qualityMap();
            long s = System.currentTimeMillis();
            DownloadedVideoInfo downloadedVideoInfo = Objects.requireNonNull(client.startDownload(qualityMap.get(VideoQuality.HQ), info)).get();
            long e = System.currentTimeMillis();
            System.out.println("Video downloaded: " + (e - s));
            System.out.println(downloadedVideoInfo);
        } catch (UnsuccessResponseException e) {
            log.error("UNSECCUESS: code: {}", e.getCode(), e);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    private static class Listener {
        int del = 1024 * 1024;

        private long totalBytes;

        @Subscribe(channel = "video.download")
        void listen(@NonNull VideoDownloadingChannel channel) {
            if (channel.getType() == DownloadingType.START) {
                totalBytes = channel.getBytes() / del;
            } else if (channel.getType() == DownloadingType.PROCESS) {
                System.out.print("\rLOADED: " + (channel.getBytes() / del) + "mb/" + totalBytes + "mb");
            } else if (channel.getType() == DownloadingType.END) {
                System.out.println("\nEnd download video");
            }
        }
    }
}