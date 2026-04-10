package com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel;

import com.plovdev.pornviewer.utils.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * Метаданные видеофайла PVVF.
 * Содержит зашифрованный JSON и Preview с их индивидуальными тегами Poly1305.
 */
public record VideoMetadata(int metadataSize, int encryptedJsonSize, int encryptedPreviewSize, byte[] metadataNonce,
                            byte[] encryptedJson, byte[] jsonTag, byte[] encryptedPreview, byte[] previewTag,
                            long metadataCRC32) {

    public static final int BASE_NONCE_LENGTH = 8;
    public static final int TAG_SIZE = 16;
    public static final int CRC_SIZE = 4;
    public static final String JSON_INDIFICATOR = "JSON";
    public static final String PREVIEW_INDIFICATOR = "PRVW";

    public VideoMetadata {
        Objects.requireNonNull(metadataNonce);
        Objects.requireNonNull(encryptedJson);
        Objects.requireNonNull(jsonTag);
        Objects.requireNonNull(encryptedPreview);
        Objects.requireNonNull(previewTag);

        if (metadataNonce.length != BASE_NONCE_LENGTH) {
            throw new IllegalArgumentException("Metadata nonce must be 8 bytes");
        }
        if (jsonTag.length != TAG_SIZE || previewTag.length != TAG_SIZE) {
            throw new IllegalArgumentException("ChaCha20 tags must be 16 bytes");
        }
    }

    /**
     * Создает VideoMetadata из минимального набора данных.
     *
     * @param encryptedJson    зашифрованная json строка с тегом, полученная из CryptoEngine.
     * @param encryptedPreview зашифрованное preview с тегом, полученное из CryptoEngine.
     * @return VideoMetadata class
     */
    public static VideoMetadata ofOnlyRequired(byte[] nonce, byte[] encryptedJson, byte[] encryptedPreview) {
        int jsonSize = encryptedJson.length - TAG_SIZE;
        int previewSize = encryptedPreview.length - TAG_SIZE;

        byte[] jsonContent = Arrays.copyOfRange(encryptedJson, 0, jsonSize);
        byte[] jsonTag = Arrays.copyOfRange(encryptedJson, jsonSize, encryptedJson.length);

        byte[] previewContent = Arrays.copyOfRange(encryptedPreview, 0, previewSize);
        byte[] previewTag = Arrays.copyOfRange(encryptedPreview, previewSize, encryptedPreview.length);

        int metadataSize = 20 + encryptedJson.length + encryptedPreview.length; // 20b metadata technical fields + content sizes
        long crc32 = calculateCRC32(metadataSize, jsonSize, previewSize, nonce, jsonContent, jsonTag, previewContent, previewTag);

        return new VideoMetadata(metadataSize, jsonSize, previewSize, nonce, jsonContent, jsonTag, previewContent, previewTag, crc32);
    }

    /**
     * Формирует полный 12-байтовый Nonce для JSON блока.
     */
    public static byte[] getJsonFullNonce(byte[] metadataNonce) {
        return ByteBuffer.allocate(12).put(metadataNonce).put(JSON_INDIFICATOR.getBytes(StandardCharsets.US_ASCII)).array();
    }

    public static byte[] jsonId() {
        return JSON_INDIFICATOR.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Формирует полный 12-байтовый Nonce для Preview блока.
     */
    public static byte[] getPreviewFullNonce(byte[] metadataNonce) {
        return ByteBuffer.allocate(12).put(metadataNonce).put(PREVIEW_INDIFICATOR.getBytes(StandardCharsets.US_ASCII)).array();
    }

    public byte[] prepareJsonToDecrypt() {
        return ByteBuffer.allocate(encryptedJsonSize + TAG_SIZE).put(encryptedJson).put(jsonTag).array();
    }
    public byte[] preparePreviewToDecrypt() {
        return ByteBuffer.allocate(encryptedPreviewSize + TAG_SIZE).put(encryptedPreview).put(previewTag).array();
    }

    public static byte[] previewId() {
        return PREVIEW_INDIFICATOR.getBytes(StandardCharsets.US_ASCII);
    }

    public long calculateCRC32() {
        return calculateCRC32(metadataSize, encryptedJsonSize, encryptedPreviewSize, metadataNonce, encryptedJson, jsonTag, encryptedPreview, previewTag);
    }

    public static long calculateCRC32(int metadataSize, int encryptedJsonSize, int encryptedPreviewSize, byte[] metadataNonce, byte[] encryptedJson, byte[] jsonTag, byte[] encryptedPreview, byte[] previewTag) {
        CRC32 crc32 = new CRC32();
        crc32.update(NumberUtils.intToBytes(metadataSize));
        crc32.update(NumberUtils.intToBytes(encryptedJsonSize));
        crc32.update(NumberUtils.intToBytes(encryptedPreviewSize));
        crc32.update(metadataNonce);
        crc32.update(encryptedJson);
        crc32.update(jsonTag);
        crc32.update(encryptedPreview);
        crc32.update(previewTag);

        return crc32.getValue();
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[metadata:start]\n");
        builder.append("metasize - ").append(metadataSize).append("\n");
        builder.append("jsonsize - ").append(encryptedJsonSize).append("\n");
        builder.append("previewsize - ").append(encryptedPreviewSize).append("\n");
        builder.append("nonce - ").append(Arrays.toString(metadataNonce)).append("\n");
        builder.append("[encrypted json]\n");
        builder.append("json tag - ").append(Arrays.toString(jsonTag)).append("\n");
        builder.append("[encrypted preview]\n");
        builder.append("preview tag - ").append(Arrays.toString(previewTag)).append("\n");
        builder.append("CRC32 - ").append(metadataCRC32).append("\n");
        builder.append("[metadata:end]\n");

        return builder.toString();
    }
}