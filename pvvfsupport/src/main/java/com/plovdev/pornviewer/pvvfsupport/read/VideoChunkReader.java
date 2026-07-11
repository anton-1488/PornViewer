package com.plovdev.pornviewer.pvvfsupport.read;

import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.security.RegisteredSecurityModule;

import javax.crypto.Cipher;
import java.io.File;

public class VideoChunkReader implements AutoCloseable {
    private final PVVFParser pvvfParser;
    private final CryptoEngine engine;

    public VideoChunkReader(File file, byte[] baseNonce) {
        pvvfParser = new PVVFParser(file);
        engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(RegisteredSecurityModule.PVVF_SUPPORT), baseNonce);
    }

    public VideoChunkReader(File file, CryptoEngine engine) {
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