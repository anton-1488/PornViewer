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
 * @param mime                 Тип контейнера (например, "MP4 ", "MKV "). Должен занимать 4 символа.
 * @param lastChunkPaddingSize Количество байт-заполнителей (padding) в последнем зашифрованном чанке.
 * @param plainVideoSize       Размер оригинального видеофайла в байтах до шифрования.
 * @param encVideoSize         Общий размер зашифрованной части видео (сумма всех чанков с тегами).
 * @param baseNonce            Базовый вектор инициализации (8 байт), используемый для формирования nonce чанков.
 */
public record VideoHeader(byte version, byte flag, String mime, int lastChunkPaddingSize, long plainVideoSize,
                          long encVideoSize, byte[] baseNonce) {
    /**
     * Фиксированный размер заголовка в байтах.
     */
    public static final int HEADER_SIZE = 34;

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

        return new VideoHeader(version, flag, mime, lastChunkPaddingSize, plainVideoSize, encVideoSize, baseNonce);
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
        builder.append("[header:end]\n");

        return builder.toString();
    }
}