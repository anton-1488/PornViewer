package com.plovdev.pornviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws Exception {
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(new File("/Users/mac/IdeaProjects/Untitled.mp4")))) {
            log.info("start writing...");
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error export video: ", e);
        }
    }
}