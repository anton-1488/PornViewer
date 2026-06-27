package com.plovdev.pornviewer;

import com.plovdev.pornviewer.http.PornHttpClient;
import org.plovdev.pvva.read.PVVAReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger("CLEAR");

    static void main() {
        try (PVVAReader reader = new PVVAReader(Path.of("/Users/mac/.PornViewer/plugins/Porn365.pvva"))) {
            try (PornHttpClient client = new PornHttpClient(reader.parseVideoAdapter())) {
                System.out.println(client.requestMainPage(0));
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}