package com.plovdev.pornviewer;

import com.plovdev.pornviewer.encryptionsupport.pvvf.read.PVVFParser;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger(PornViewer.class);

    void main(String[] args) {
        try (PVVFParser parser = new PVVFParser(Path.of("/Users/mac/.PornViewer/downloads/3cf12893ec4f2b6a1483915960b30cf2140940d7face762e49fd4c518a680ac3"))) {
            long s = System.currentTimeMillis();
            VideoHeader header = parser.parseVideoHeader();
            long e = System.currentTimeMillis();
            log.info("Parsing time: {}", e - s);

            System.out.println(header);
        } catch (Exception e) {
            log.error("Error parsing: ", e);
        }
    }
}