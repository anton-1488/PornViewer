package com.plovdev.pornviewer;

import com.plovdev.pornviewer.http.PornClient;
import com.plovdev.pornviewer.http.PornClientImpl;
import org.plovdev.pvva.read.DefaultPVVAReader;
import org.plovdev.pvva.read.PVVAReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger(PornViewer.class);

    static void main() {
        try (PVVAReader reader = new DefaultPVVAReader(Path.of("/Users/mac/.PornViewer/plugins/Porn365.pvva"));
             PornClient client = new PornClientImpl(reader.readVideoAdapter())) {
            System.out.println(client.requestMainPage(0));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}