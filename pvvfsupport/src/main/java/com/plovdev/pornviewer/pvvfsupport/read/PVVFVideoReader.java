package com.plovdev.pornviewer.pvvfsupport.read;

import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.security.RegisteredSecurityModule;
import com.plovdev.pornviewer.services.json.DownloadedVideoInfo;
import com.plovdev.pornviewer.services.json.VideoInfoSerializer;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class PVVFVideoReader {
    public static VideoHeader readHeader(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.parseVideoHeader();
        }
    }

    public static VideoMetadata readMetadata(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.parseVideoMetadata();
        }
    }

    public static EncryptedVideo readVideo(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.collectEncryptedVideo();
        }
    }

    public static @NotNull DownloadedVideoInfo readInfo(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            EncryptedVideo video = pvvfParser.collectEncryptedVideo();
            VideoMetadata metadata = video.getVideoMetadata();
            CryptoEngine engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(RegisteredSecurityModule.PVVF_SUPPORT), metadata.metadataNonce());

            byte[] decryptedJson = engine.processData(metadata.prepareJsonToDecrypt(), VideoMetadata.getJsonFullNonce(metadata.metadataNonce()), VideoMetadata.jsonId());
            String json = new String(decryptedJson, StandardCharsets.UTF_8);

            DownloadedVideoInfo info = VideoInfoSerializer.deserializeInfo(json);

            byte[] decryptedPreview = engine.processData(metadata.preparePreviewToDecrypt(), VideoMetadata.getPreviewFullNonce(metadata.metadataNonce()), VideoMetadata.previewId());
            info.setPreviewBytes(decryptedPreview);

            return info;
        }
    }
}