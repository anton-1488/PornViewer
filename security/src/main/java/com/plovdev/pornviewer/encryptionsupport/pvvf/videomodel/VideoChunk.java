package com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel;

import java.util.Objects;

/**
 * Представляет собой зашифрованный фрагмент видеофайла.
 * <p>
 * Структура чанка соответствует алгоритму ChaCha20-Poly1305, где данные
 * и аутентификационный тег хранятся раздельно, но могут быть объединены
 * для обработки через стандартный Java Cipher.
 * </p>
 *
 * @param chunkNumber   Порядковый номер чанка (используется для формирования nonce).
 * @param encryptedData Зашифрованные данные видео (строго {@value #PLAIN_CHUNK_SIZE} байт).
 * @param tag           Аутентификационный тег Poly1305 (строго {@value #TAG_SIZE} байт).
 */
public record VideoChunk(long chunkNumber, byte[] encryptedData, byte[] tag) {
    /**
     * Размер исходных данных в чанке - 128 КБ.
     */
    public static final int PLAIN_CHUNK_SIZE = 128 * 1024;

    /**
     * Размер тега аутентификации Poly1305 - 16 байт.
     */
    public static final int TAG_SIZE = 16;

    /**
     * Общий размер зашифрованного блока (данные + тег).
     */
    public static final int TOTAL_CHUNK_SIZE = PLAIN_CHUNK_SIZE + TAG_SIZE;

    /**
     * Компактный конструктор для валидации целостности структуры чанка.
     *
     * @throws NullPointerException     если данные или тег равны null.
     * @throws IllegalArgumentException если размер тега не равен 16 байт или размер данных не равен 128 КБ.
     */
    public VideoChunk {
        Objects.requireNonNull(encryptedData, "Encrypted data cannot be null");
        Objects.requireNonNull(tag, "Tag cannot be null");

        if (tag.length != TAG_SIZE) {
            throw new IllegalArgumentException("Illegal tag size: expected " + TAG_SIZE + " bytes");
        }
        if (encryptedData.length != PLAIN_CHUNK_SIZE) {
            throw new IllegalArgumentException("Illegal chunk size: expected " + PLAIN_CHUNK_SIZE + " bytes");
        }
    }

    public static VideoChunk ofEncryptedWithTag(long chunkNumber, byte[] chunkData) {
        byte[] content = new byte[PLAIN_CHUNK_SIZE];
        byte[] tag = new byte[TAG_SIZE];

        System.arraycopy(chunkData, 0, content, 0, PLAIN_CHUNK_SIZE);
        System.arraycopy(chunkData, PLAIN_CHUNK_SIZE, tag, 0, TAG_SIZE);

        return new VideoChunk(chunkNumber, content, tag);
    }

    /**
     * Объединяет зашифрованные данные и тег в единый массив байтов.
     * <p>
     * Данный формат (Ciphertext + Tag) является стандартным для реализации
     * ChaCha20-Poly1305 в Java (JCE). Результат этого метода можно напрямую
     * передавать в Cipher для расшифровки.
     *
     * @return Массив байтов размером {@value #TOTAL_CHUNK_SIZE}.
     */
    public byte[] prepareChunk() {
        byte[] input = new byte[TOTAL_CHUNK_SIZE];
        System.arraycopy(encryptedData, 0, input, 0, PLAIN_CHUNK_SIZE);
        System.arraycopy(tag, 0, input, PLAIN_CHUNK_SIZE, TAG_SIZE);
        return input;
    }
}