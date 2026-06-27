package com.plovdev.pornviewer.http;

import com.google.gson.Gson;
import com.plovdev.pornviewer.core.http.PornRequest;
import com.plovdev.pornviewer.core.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.http.providers.PornRequestProvider;
import com.plovdev.pornviewer.core.exceptions.VideoDownloadingError;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata;
import com.plovdev.pornviewer.pvvfsupport.write.PVVFWriter;
import com.plovdev.pornviewer.security.CipherEngineUtils;
import com.plovdev.pornviewer.security.CryptoEngine;
import com.plovdev.pornviewer.security.DigestUtils;
import com.plovdev.pornviewer.security.PVSecurityManager;
import com.plovdev.pornviewer.core.events.GlobalEventManager;
import com.plovdev.pornviewer.core.events.VideoDownloadingChannel;
import com.plovdev.pornviewer.services.files.PVFileManager;
import com.plovdev.pornviewer.services.json.DownloadedVideoInfo;
import com.plovdev.pornviewer.services.json.VideoInfoSerializer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk.PLAIN_CHUNK_SIZE;

public class PornDownloader {
    private static final Logger log = LoggerFactory.getLogger(PornDownloader.class);
    private static final Gson GSON = new Gson();
    private final PornRequest request;
    private final File output;
    private final PornRequestProvider requestProvider;

    public PornDownloader(PornRequestProvider provider, PornRequest request, String filename) {
        this.request = request;
        this.requestProvider = provider;
        String encryptedFileName = DigestUtils.sha256(filename);
        this.output = new File(PVFileManager.getPvDownloadsPath() + (File.separatorChar + encryptedFileName));
    }

    public synchronized CompletableFuture<DownloadedVideoInfo> startDownload(long plainVideoSize, FullVideoInfo info, byte[] videoPreview) {
        return CompletableFuture.supplyAsync(() -> {
            // open pvvf writer to write data
            try (PVVFWriter writer = new PVVFWriter(output)) {
                // step 1 - write pvvf header:
                VideoHeader header = prepareAndWriteHeader(writer, plainVideoSize);

                // step 2 - init cipher engine
                // get ready to encrypt video chunks
                CryptoEngine engine = new CryptoEngine(Cipher.ENCRYPT_MODE, PVSecurityManager.getPassword(), header.baseNonce());

                // step 3 - load, encrypt and save video chunks:
                loadAndSaveVideoChunks(writer, engine);

                // step 4 - write pvvf metadata and close writer:
                prepareAndWriteMetadata(writer, info, videoPreview);
                GlobalEventManager.broadcastEvent(new VideoDownloadingChannel(plainVideoSize, VideoDownloadingChannel.DownloadedType.END));
                return new DownloadedVideoInfo(info.title(), info.description(), info.videoUri().toString(), info.timecodes(), info.tagLinks().keySet().stream().toList(), info.videoDuration());
            } catch (Exception e) {
                GlobalEventManager.broadcastEvent(new VideoDownloadingChannel(e, VideoDownloadingChannel.DownloadedType.ERROR));
                log.error("Error to download video: ", e);
                throw new VideoDownloadingError("Error to download video: ", e);
            }
        });
    }

    private @NonNull VideoHeader prepareAndWriteHeader(@NonNull PVVFWriter writer, long videSize) {
        long remainder = videSize % PLAIN_CHUNK_SIZE;
        int lastChunkPaddingSize = (remainder == 0) ? 0 : (int) (PLAIN_CHUNK_SIZE - remainder);
        String mimeType = PVFileManager.guessMimeType(request.path().toString());
        VideoHeader header = VideoHeader.ofOnlyRequired(mimeType, lastChunkPaddingSize, videSize);
        writer.writeVideoHeader(header);
        return header;
    }

    private void loadAndSaveVideoChunks(PVVFWriter writer, CryptoEngine engine) {
        try {
            try (InputStream readStream = requestProvider.requestStream(request)) {
                byte[] chunkBuffer = new byte[PLAIN_CHUNK_SIZE];
                long totalReaded = 0;
                int readed;
                long chunkIndex = 0;

                while ((readed = readStream.readNBytes(chunkBuffer, 0, PLAIN_CHUNK_SIZE)) > 0) {
                    byte[] plainChunk = chunkBuffer;
                    if (readed < PLAIN_CHUNK_SIZE) {
                        plainChunk = new byte[PLAIN_CHUNK_SIZE];
                        System.arraycopy(chunkBuffer, 0, plainChunk, 0, readed);
                    }
                    byte[] encryptedWithTag = engine.processChunk(chunkIndex, plainChunk);
                    writer.appendVideoChunk(VideoChunk.ofEncryptedWithTag(chunkIndex, encryptedWithTag));
                    GlobalEventManager.broadcastEvent(new VideoDownloadingChannel(totalReaded, VideoDownloadingChannel.DownloadedType.PROCESS));
                    totalReaded += readed;
                    chunkIndex++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareAndWriteMetadata(@NonNull PVVFWriter writer, FullVideoInfo info, byte[] preview) {
        byte[] nonce = new byte[VideoMetadata.BASE_NONCE_LENGTH];
        CipherEngineUtils.createRandomPassword(nonce);
        CryptoEngine engine = new CryptoEngine(Cipher.ENCRYPT_MODE, PVSecurityManager.getPassword(), nonce); // update nonce to encrypt metadata

        String json = formJson(info);
        byte[] jsonNonce = VideoMetadata.getJsonFullNonce(nonce);
        byte[] jsonId = VideoMetadata.jsonId();
        byte[] encryptedJson = engine.processData(json.getBytes(StandardCharsets.UTF_8), jsonNonce, jsonId);

        byte[] previewNonce = VideoMetadata.getPreviewFullNonce(nonce);
        byte[] previewId = VideoMetadata.previewId();
        byte[] encryptedPreview = engine.processData(preview, previewNonce, previewId);
        VideoMetadata metadata = VideoMetadata.ofOnlyRequired(nonce, encryptedJson, encryptedPreview);
        writer.writeVideoMetadata(metadata);
    }

    private String formJson(FullVideoInfo info) {
        return VideoInfoSerializer.serializeInfo(info);
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public PornRequest getRequest() {
        return request;
    }

    public File getOutput() {
        return output;
    }
}