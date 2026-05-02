package com.plovdev.pornviewer.server.decryptionimpl;

import com.plovdev.pornviewer.encryptionsupport.videoparser.read.VideoChunkReader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk;
import com.plovdev.pornviewer.server.utils.VideoRequestSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

public class ServerDecryptedStreamer {
    private static final Logger log = LoggerFactory.getLogger(ServerDecryptedStreamer.class);
    private final File file;
    private final long startPosition;
    private final long length;
    private final EncryptedVideo video;
    private final VideoChunkReader chunkReader;

    public ServerDecryptedStreamer(File file, EncryptedVideo video, long startInFile, long length, VideoRequestSet set) {
        this.file = file;
        this.startPosition = startInFile;
        this.length = length;
        this.video = video;
        chunkReader = new VideoChunkReader(file, set.getCryptoEngine());
    }

    public File getFile() {
        return file;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getLength() {
        return length;
    }

    public EncryptedVideo getVideo() {
        return video;
    }

    public VideoChunkReader getChunkReader() {
        return chunkReader;
    }

    public void transferToOutput(BufferedOutputStream stream) {
        try {
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