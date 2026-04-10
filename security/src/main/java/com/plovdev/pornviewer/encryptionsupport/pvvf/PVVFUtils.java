package com.plovdev.pornviewer.encryptionsupport.pvvf;

import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk;

public class PVVFUtils {
    private PVVFUtils() {
        throw new UnsupportedOperationException();
    }

    public static long calculateTotalEncVideoSize(long plainSize) {
        long totalPlainChunks = Math.ceilDiv(plainSize, VideoChunk.PLAIN_CHUNK_SIZE);
        return VideoChunk.TOTAL_CHUNK_SIZE * totalPlainChunks;
    }

    public static long calculateTotalChunksInPlainVideo(long plainSize) {
        return Math.ceilDiv(plainSize, VideoChunk.PLAIN_CHUNK_SIZE);
    }

    public static long calculateTotalChunksInEncVideo(long encSize) {
        return Math.ceilDiv(encSize, VideoChunk.TOTAL_CHUNK_SIZE);
    }

    public static long calculateTotalPlainVideoSize(long encSize) {
        long totalEncChunks = Math.ceilDiv(encSize, VideoChunk.TOTAL_CHUNK_SIZE);
        long totalTagsSize = totalEncChunks * VideoChunk.TAG_SIZE;
        if (encSize < totalTagsSize) return 0;

        return encSize - totalTagsSize;
    }
}