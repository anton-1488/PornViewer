package com.plovdev.pornviewer.encryptionsupport.pvvf.read;

import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;

import javax.crypto.Cipher;
import java.nio.file.Path;

public class VideoChunkReader implements AutoCloseable {
    private final PVVFParser pvvfParser;
    private final CryptoEngine engine;

    public VideoChunkReader(Path file, byte[] baseNonce) {
        pvvfParser = new PVVFParser(file);
        engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(), baseNonce);
    }

    public VideoChunkReader(Path file, CryptoEngine engine) {
        pvvfParser = new PVVFParser(file);
        this.engine = engine;
    }

    public byte[] readEncryptedChunk(long chunkIndex) {
        VideoChunk chunk = pvvfParser.parseVideoChunk(chunkIndex);
        return engine.processChunk(chunkIndex, chunk.prepareChunk());
    }

    @Override
    public void close() {
        pvvfParser.close();
    }
}