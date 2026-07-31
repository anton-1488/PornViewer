package com.plovdev.pornviewer.server.handlers.helpers;

import com.plovdev.pornviewer.pvvfsupport.read.VideoChunkReader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk;
import com.plovdev.pornviewer.server.models.VideoRequestSet;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

public class VideoWatchHelper {
    private static final Logger log = LoggerFactory.getLogger(VideoWatchHelper.class);

    public static void transferEncryptedStream(File requestedFile, @NonNull VideoRequestSet requestSet, long startPosition, long length, BufferedOutputStream stream) {
        try (VideoChunkReader chunkReader = new VideoChunkReader(requestedFile, requestSet.cryptoEngine())) {
            long startChunk = startPosition / VideoChunk.PLAIN_CHUNK_SIZE;
            long endChunk = (startPosition + length - 1) / VideoChunk.PLAIN_CHUNK_SIZE;
            long offsetInStartChunk = startPosition % VideoChunk.PLAIN_CHUNK_SIZE;
            long remaining = length;

            for (long i = startChunk; i <= endChunk && remaining > 0; i++) {
                byte[] plainChunk = chunkReader.readEncryptedChunk(i);
                long start = (i == startChunk) ? offsetInStartChunk : 0;

                long availableInChunk = plainChunk.length - start;
                long toWrite = Math.min(availableInChunk, remaining);
                if (toWrite > 0) {
                    stream.write(plainChunk, (int) start, (int) toWrite);
                    remaining -= toWrite;
                }
            }
            if (remaining > 0) {
                log.warn("Underflow! Still need to write {} bytes", remaining);
            }
            stream.flush();
        } catch (IOException e) {
            log.debug("Error to transfer video: {}", e.getMessage());
        }
    }
}