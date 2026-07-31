package com.plovdev.pornviewer.pvvfsupport.videomodel;

import com.plovdev.pornviewer.pvvfsupport.PVVFUtils;
import com.plovdev.pornviewer.security.CryptoUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Представляет заголовок видеофайла формата PVVF.
 * <p>
 * Общий размер заголовка составляет {@value #HEADER_SIZE} байта.
 * Структура: Magic(4), Version(1), Flag(1), MIME(4), Padding size(4), PlainSize(8), EncSize(8), Nonce(8), CRC32(4).
 * </p>
 *
 * @param version              Версия формата файла (по умолчанию {@value #DEFAULT_VERSION}).
 * @param flag                 Системные флаги (например, наличие метаданных в конце файла).
 * @param mimeLength           Длина mime-type строки.
 * @param lastChunkPaddingSize Количество байт-заполнителей (padding) в последнем зашифрованном чанке.
 * @param plainVideoSize       Размер оригинального видеофайла в байтах до шифрования.
 * @param encVideoSize         Общий размер зашифрованной части видео (сумма всех чанков с тегами).
 * @param baseNonce            Базовый вектор инициализации (8 байт), используемый для формирования nonce чанков.
 * @param mime                 Тип контейнера.
 */
public record VideoHeader(byte version, byte flag, byte mimeLength, int lastChunkPaddingSize,
                          long plainVideoSize,
                          long encVideoSize, byte[] baseNonce, String mime) {
    /**
     * Фиксированный размер заголовка в байтах.
     */
    public static final int HEADER_SIZE = 31;

    public static final int ENC_VIDEO_SIZE_OFFSET = 22;

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

    @Contract("_, _, _ -> new")
    public static @NonNull VideoHeader ofOnlyRequired(String mime, int lastChunkPaddingSize, long plainVideoSize) {
        byte version = 1;
        byte flag = 0;

        byte[] baseNonce = new byte[8];
        CryptoUtils.createRandomPassword(baseNonce);
        long encVideoSize = PVVFUtils.calculateTotalEncVideoSize(plainVideoSize);

        return new VideoHeader(version, flag, (byte) mime.length(), lastChunkPaddingSize, plainVideoSize, encVideoSize, baseNonce, mime);
    }

    /**
     * Возвращает магическое число формата.
     *
     * @return Строка {@value #MAGIC_NUMBER}.
     */
    public String magic() {
        return MAGIC_NUMBER;
    }

    @NotNull
    @Override
    public String toString() {
        return "[header:start]\n" +
                "[" + MAGIC_NUMBER + "]\n" +
                "version - " + version + "\n" +
                "flag - " + flag + "\n" +
                "mime - " + mime + "\n" +
                "LCPS - " + lastChunkPaddingSize + "\n" +
                "plain size - " + plainVideoSize + "\n" +
                "enc size - " + encVideoSize + "\n" +
                "nonce - " + Arrays.toString(baseNonce) + "\n" +
                "[header:end]\n";
    }
}