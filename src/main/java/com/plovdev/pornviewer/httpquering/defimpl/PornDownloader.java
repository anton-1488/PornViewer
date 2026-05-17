package com.plovdev.pornviewer.httpquering.defimpl;

import com.google.gson.Gson;
import com.plovdev.pornviewer.encryptionsupport.CipherEngineUtils;
import com.plovdev.pornviewer.encryptionsupport.CryptoEngine;
import com.plovdev.pornviewer.encryptionsupport.DigestUtils;
import com.plovdev.pornviewer.encryptionsupport.LoadersUtils;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoMetadata;
import com.plovdev.pornviewer.encryptionsupport.videoparser.write.PVVFWriter;
import com.plovdev.pornviewer.events.listeners.FileDownloadingListener;
import com.plovdev.pornviewer.httpquering.PornRequest;
import com.plovdev.pornviewer.httpquering.PornRequestProvider;
import com.plovdev.pornviewer.models.VideoInfo;
import com.plovdev.pornviewer.utility.files.FileUtils;
import com.plovdev.pornviewer.utility.json.VideoInfoSerializer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk.PLAIN_CHUNK_SIZE;

public class PornDownloader {
    private static final Logger log = LoggerFactory.getLogger(PornDownloader.class);
    private static final Gson GSON = new Gson();
    private URI uri;
    private final File toWriteFile;
    private PornRequestProvider requestProvider;

    public PornDownloader(PornRequestProvider provider, URI uri, String filename) {
        this.uri = uri;
        this.requestProvider = provider;
        String encryptedFileName = DigestUtils.sha256(filename);
        this.toWriteFile = new File(new FileUtils().getPvDownloadsPath() + (File.separatorChar + encryptedFileName));
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public PornRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public void setRequestProvider(PornRequestProvider requestProvider) {
        this.requestProvider = requestProvider;
    }

    public File getToWriteFile() {
        return toWriteFile;
    }

    public synchronized void startDownload(long plainVideoSize, VideoInfo info, byte[] videoPreview) {
        log.info("Write file to: {}", toWriteFile.toString());
        FileDownloadingListener.notifyStartsListeners(plainVideoSize);

        // open pvvf writer to write data
        try (PVVFWriter writer = new PVVFWriter(toWriteFile)) {
            // step 1 - write pvvf header:
            VideoHeader header = prepareAndWriteHeader(writer, plainVideoSize);

            // step 2 - init cipher engine
            // get ready to encrypt video chunks
            CryptoEngine engine = new CryptoEngine(Cipher.ENCRYPT_MODE, CipherEngineUtils.getPassword().toCharArray(), header.baseNonce());

            // step 3 - load, encrypt and save video chunks:
            loadAndSaveVideoChunks(writer, engine);

            // step 4 - write pvvf metadata and close writer:
            prepareAndWriteMetadata(writer, engine, info, videoPreview);
            FileDownloadingListener.notifyEndListeners(toWriteFile);
        } catch (Exception e) {
            FileDownloadingListener.notifyErrorListeners(e);
            log.error("Error to download video: ", e);
        }
    }

    private @NonNull VideoHeader prepareAndWriteHeader(@NonNull PVVFWriter writer, long videSize) {
        long remainder = videSize % PLAIN_CHUNK_SIZE;
        int lastChunkPaddingSize = (remainder == 0) ? 0 : (int) (PLAIN_CHUNK_SIZE - remainder);
        String mimeType = LoadersUtils.guessMimeType(uri.toString());

        VideoHeader header = VideoHeader.ofOnlyRequired(mimeType, lastChunkPaddingSize, videSize);
        writer.writeVideoHeader(header);
        return header;
    }

    private void loadAndSaveVideoChunks(PVVFWriter writer, CryptoEngine engine) {
        try {
            log.info("Start writing chunks, uri: {}", uri.toString());
            try (InputStream readStream = requestProvider.requestStream(PornRequest.get(uri.toString()))) {
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
                    FileDownloadingListener.notifyProcessListeners(totalReaded);
                    totalReaded += readed;
                    chunkIndex++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareAndWriteMetadata(PVVFWriter writer, CryptoEngine engine, VideoInfo info, byte[] preview) {
        byte[] nonce = new byte[VideoMetadata.BASE_NONCE_LENGTH];
        CipherEngineUtils.createRandomPassword(nonce);
        engine.setBaseNonce(CipherEngineUtils.getPassword().toCharArray(), nonce); // update nonce to encrypt metadata

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

    private String formJson(VideoInfo info) {
        return VideoInfoSerializer.serializeInfo(info);
    }
}