package com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel;

import com.plovdev.pornviewer.encryptionsupport.pvvf.PVVFUtils;
import com.plovdev.pornviewer.security.CipherEngineUtils;
import com.plovdev.pornviewer.utils.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * Представляет заголовок видеофайла формата PVVF.
 * <p>
 * Общий размер заголовка составляет {@value #HEADER_SIZE} байта.
 * Структура: Magic(4), Version(1), Flag(1), MIME(4), Padding size(4), PlainSize(8), EncSize(8), Nonce(8), CRC32(4).
 * </p>
 *
 * @param version          Версия формата файла (по умолчанию {@value #DEFAULT_VERSION}).
 * @param flag             Системные флаги (например, наличие метаданных в конце файла).
 * @param mime             Тип контейнера (например, "MP4 ", "MKV "). Должен занимать 4 символа.
 * @param lastChunkPaddingSize Количество байт-заполнителей (padding) в последнем зашифрованном чанке.
 * @param plainVideoSize   Размер оригинального видеофайла в байтах до шифрования.
 * @param encVideoSize     Общий размер зашифрованной части видео (сумма всех чанков с тегами).
 * @param baseNonce        Базовый вектор инициализации (8 байт), используемый для формирования nonce чанков.
 * @param headerCRC32      Контрольная сумма первых 38 байт заголовка для проверки целостности.
 */
public record VideoHeader(byte version, byte flag, String mime, int lastChunkPaddingSize, long plainVideoSize,
                          long encVideoSize, byte[] baseNonce, long headerCRC32) {
    /**
     * Фиксированный размер заголовка в байтах.
     */
    public static final int HEADER_SIZE = 42;

    /**
     * Магическое число файла: "PVVF".
     */
    public static final String MAGIC_NUMBER = "PVVF";

    /**
     * Версия формата по умолчанию.
     */
    public static final byte DEFAULT_VERSION = 1;

    /**
     * Длина базового nonce в байтах.
     */
    public static final int BASE_NONCE_LENGTH = 8;

    /**
     * Длина поля контрольной суммы в байтах.
     */
    public static final int HEADER_CRC32_LENGTH = 4;

    /**
     * Компактный конструктор для валидации параметров заголовка.
     *
     * @throws NullPointerException     если mime, baseNonce или headerCRC32 равны null.
     * @throws IllegalArgumentException если размеры массивов nonce или CRC32 некорректны.
     */
    public VideoHeader {
        Objects.requireNonNull(mime, "MIME type cannot be null");
        Objects.requireNonNull(baseNonce, "Base nonce cannot be null");

        if (baseNonce.length != BASE_NONCE_LENGTH) {
            throw new IllegalArgumentException("Illegal nonce size: " + baseNonce.length + "/" + BASE_NONCE_LENGTH);
        }
    }

    public static VideoHeader ofOnlyRequired(String mime, int lastChunkPaddingSize, long plainVideoSize) {
        byte version = 1;
        byte flag = 0;

        byte[] baseNonce = new byte[8];
        CipherEngineUtils.createRandomPassword(baseNonce);

        long encVideoSize = PVVFUtils.calculateTotalEncVideoSize(plainVideoSize);
        long crc32 = VideoHeader.calculateCRC32(version, flag, mime, lastChunkPaddingSize, plainVideoSize, encVideoSize, baseNonce);

        return new VideoHeader(version, flag, mime, lastChunkPaddingSize, plainVideoSize, encVideoSize, baseNonce, crc32);
    }

    /**
     * Возвращает магическое число формата.
     *
     * @return Строка {@value #MAGIC_NUMBER}.
     */
    public String magic() {
        return MAGIC_NUMBER;
    }

    public long calculateCRC32() {
        return calculateCRC32(version, flag, mime, lastChunkPaddingSize, plainVideoSize, encVideoSize, baseNonce);
    }
    public static long calculateCRC32(byte version, byte flag, String mime, int lastChunkPaddingSize, long plainVideoSize, long encVideoSize, byte[] baseNonce) {
        CRC32 crc32 = new CRC32();
        crc32.update(MAGIC_NUMBER.getBytes(StandardCharsets.US_ASCII));
        crc32.update(version);
        crc32.update(flag);
        crc32.update(mime.getBytes(StandardCharsets.US_ASCII));
        crc32.update(NumberUtils.intToBytes(lastChunkPaddingSize));
        crc32.update(NumberUtils.longToBytes(plainVideoSize));
        crc32.update(NumberUtils.longToBytes(encVideoSize));
        crc32.update(baseNonce);

        return crc32.getValue();
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[header:start]\n");
        builder.append("[").append(MAGIC_NUMBER).append("]\n");
        builder.append("version - ").append(version).append("\n");
        builder.append("flag - ").append(flag).append("\n");
        builder.append("mime - ").append(mime).append("\n");
        builder.append("LCPS - ").append(lastChunkPaddingSize).append("\n");
        builder.append("plain size - ").append(plainVideoSize).append("\n");
        builder.append("enc size - ").append(encVideoSize).append("\n");
        builder.append("nonce - ").append(Arrays.toString(baseNonce)).append("\n");
        builder.append("CRC32 - ").append(headerCRC32).append("\n");
        builder.append("[header:end]\n");

        return builder.toString();
    }
}