package com.plovdev.pornviewer.encryptionsupport.pvvf.read;

import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoMetadata;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.utils.json.DownloadedVideoInfo;
import com.plovdev.pornviewer.utils.json.VideoInfoSerializer;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class PVVFVideoReader {
    public static VideoHeader readHeader(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.parseVideoHeader();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static VideoMetadata readMetadata(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.collectEncryptedVideo().getVideoMetadata();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EncryptedVideo readVideo(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            return pvvfParser.collectEncryptedVideo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DownloadedVideoInfo readInfo(File file) {
        try (PVVFParser pvvfParser = new PVVFParser(file)) {
            EncryptedVideo video = pvvfParser.collectEncryptedVideo();
            VideoMetadata metadata = video.getVideoMetadata();
            CryptoEngine engine = new CryptoEngine(Cipher.DECRYPT_MODE, PVSecurityManager.getPassword(), metadata.metadataNonce());

            byte[] decryptedJson = engine.processData(metadata.prepareJsonToDecrypt(), VideoMetadata.getJsonFullNonce(metadata.metadataNonce()), VideoMetadata.jsonId());
            String json = new String(decryptedJson, StandardCharsets.UTF_8);

            DownloadedVideoInfo info = VideoInfoSerializer.deserializeInfo(json);

            byte[] decryptedPreview = engine.processData(metadata.preparePreviewToDecrypt(), VideoMetadata.getPreviewFullNonce(metadata.metadataNonce()), VideoMetadata.previewId());
            info.setPreviewBytes(decryptedPreview);

            return info;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}