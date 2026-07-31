package com.plovdev.pornviewer.pvvfsupport.videomodel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Метаданные видеофайла PVVF.
 * Содержит зашифрованный JSON и Preview с их индивидуальными тегами Poly1305.
 */
public record VideoMetadata(int metadataSize, int encryptedJsonSize, int encryptedPreviewSize, byte[] metadataNonce,
                            byte[] encryptedJson, byte[] jsonTag, byte[] encryptedPreview, byte[] previewTag) {

    public static final int BASE_NONCE_LENGTH = 8;
    public static final int TAG_SIZE = 16;
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
    public static @NonNull VideoMetadata ofOnlyRequired(byte[] nonce, byte @NonNull [] encryptedJson, byte @NonNull [] encryptedPreview) {
        int jsonSize = encryptedJson.length - TAG_SIZE;
        int previewSize = encryptedPreview.length - TAG_SIZE;

        byte[] jsonContent = Arrays.copyOfRange(encryptedJson, 0, jsonSize);
        byte[] jsonTag = Arrays.copyOfRange(encryptedJson, jsonSize, encryptedJson.length);

        byte[] previewContent = Arrays.copyOfRange(encryptedPreview, 0, previewSize);
        byte[] previewTag = Arrays.copyOfRange(encryptedPreview, previewSize, encryptedPreview.length);
        int metadataSize = 20 + encryptedJson.length + encryptedPreview.length; // 20b metadata technical fields + content sizes

        return new VideoMetadata(metadataSize, jsonSize, previewSize, nonce, jsonContent, jsonTag, previewContent, previewTag);
    }

    /**
     * Формирует полный 12-байтовый Nonce для JSON блока.
     */
    public static byte @NonNull [] getJsonFullNonce(byte[] metadataNonce) {
        return ByteBuffer.allocate(12).put(metadataNonce).put(JSON_INDIFICATOR.getBytes(StandardCharsets.US_ASCII)).array();
    }

    @Contract(pure = true)
    public static byte @NonNull [] jsonId() {
        return JSON_INDIFICATOR.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Формирует полный 12-байтовый Nonce для Preview блока.
     */
    public static byte @NonNull [] getPreviewFullNonce(byte[] metadataNonce) {
        return ByteBuffer.allocate(12).put(metadataNonce).put(PREVIEW_INDIFICATOR.getBytes(StandardCharsets.US_ASCII)).array();
    }

    public byte @NonNull [] prepareJsonToDecrypt() {
        return ByteBuffer.allocate(encryptedJsonSize + TAG_SIZE).put(encryptedJson).put(jsonTag).array();
    }

    public byte @NonNull [] preparePreviewToDecrypt() {
        return ByteBuffer.allocate(encryptedPreviewSize + TAG_SIZE).put(encryptedPreview).put(previewTag).array();
    }

    @Contract(pure = true)
    public static byte @NonNull [] previewId() {
        return PREVIEW_INDIFICATOR.getBytes(StandardCharsets.US_ASCII);
    }

    @NotNull
    @Override
    public String toString() {
        return "[metadata:start]\n" +
                "metasize - " + metadataSize + "\n" +
                "jsonsize - " + encryptedJsonSize + "\n" +
                "previewsize - " + encryptedPreviewSize + "\n" +
                "nonce - " + Arrays.toString(metadataNonce) + "\n" +
                "[encrypted json]\n" +
                "json tag - " + Arrays.toString(jsonTag) + "\n" +
                "[encrypted preview]\n" +
                "preview tag - " + Arrays.toString(previewTag) + "\n" +
                "[metadata:end]\n";
    }
}