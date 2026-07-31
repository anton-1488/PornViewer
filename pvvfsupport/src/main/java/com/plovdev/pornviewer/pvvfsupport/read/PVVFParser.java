package com.plovdev.pornviewer.pvvfsupport.read;

import com.plovdev.pornviewer.pvvfsupport.exceptions.PVVFException;
import com.plovdev.pornviewer.pvvfsupport.exceptions.PVVFOpenException;
import com.plovdev.pornviewer.pvvfsupport.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader;
import com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk.PLAIN_CHUNK_SIZE;
import static com.plovdev.pornviewer.pvvfsupport.videomodel.VideoChunk.TOTAL_CHUNK_SIZE;
import static com.plovdev.pornviewer.pvvfsupport.videomodel.VideoHeader.*;
import static com.plovdev.pornviewer.pvvfsupport.videomodel.VideoMetadata.TAG_SIZE;

/**
 * Парсер для работы с зашифрованными видеофайлами формата PVVF.
 * <p>
 * Класс обеспечивает чтение заголовка и метаданных файла, используя произвольный доступ (RandomAccess).
 * Реализует интерфейс {@link AutoCloseable} для корректного освобождения системных ресурсов.
 * </p>
 */
public class PVVFParser implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PVVFParser.class);

    /**
     * Charset to string decoding
     */
    private static final Charset IO_CHARSET = StandardCharsets.US_ASCII;

    /**
     * Source, from parser will read data.
     */
    private final File file;
    private final RandomAccessFile RAF;

    /**
     * Создает экземпляр парсера для указанного файла.
     *
     * @param file Файл в формате PVVF для анализа.
     */
    public PVVFParser(File file) {
        this.file = file;
        try {
            RAF = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new PVVFOpenException("PVVF not found", e);
        }
    }

    public File getFile() {
        return file;
    }

    /**
     * Выполняет чтение и парсинг заголовка видеофайла (первые 42 байта).
     *
     * @return Объект {@link VideoHeader} с техническими параметрами видео.
     * @throws PVVFException если структура заголовка нарушена или файл поврежден.
     */
    public synchronized VideoHeader parseVideoHeader() {
        final byte[] FOUR_BYTE_ARRAY = new byte[4];

        try {
            // always setup RAF to file start
            RAF.seek(0);

            // step 1 - check magic number:
            String readedMagic = readString(FOUR_BYTE_ARRAY);
            if (!readedMagic.equals(MAGIC_NUMBER)) {
                throw new IOException("Illegal magic number in file: " + readedMagic);
            }

            // step 2 - read version:
            byte fileVersion = RAF.readByte();
            // step 3 - read flag:
            byte flag = RAF.readByte();

            // step 4 - read video mime type length:
            byte mimeLength = RAF.readByte();

            // step 5 - reading sizes:
            int lastChunkPaddingSize = RAF.readInt();
            long plainVideoSize = RAF.readLong();
            long encryptedVideoSize = RAF.readLong();

            // step 6 - read nonce and crc:
            byte[] baseNonce = new byte[BASE_NONCE_LENGTH];
            readToByteArray(baseNonce);

            // step 7 - check if file pointer is equal header size:
            if (RAF.getFilePointer() != HEADER_SIZE) {
                throw new IOException("Error parse file header: invalid pointer: " + RAF.getFilePointer());
            }

            // step 8 - read mime type
            byte[] mimeTypeBytes = new byte[mimeLength];
            String mimeType = readString(mimeTypeBytes);

            // collecting results and return VideoHeader class
            return new VideoHeader(fileVersion, flag, mimeLength, lastChunkPaddingSize, plainVideoSize, encryptedVideoSize, baseNonce, mimeType);
        } catch (IOException e) {
            throw new PVVFException("Error to read pvvf header", e);
        }
    }

    /**
     * Выполняет чтение метаданных, расположенных после зашифрованного тела видео.
     * Использует переданный заголовок для вычисления смещения блока метаданных.
     *
     * @return Объект {@link VideoMetadata} или null, если заголовок отсутствует.
     */
    public synchronized VideoMetadata parseVideoMetadata() {
        try {
            RAF.seek(ENC_VIDEO_SIZE_OFFSET);
            long encVideoSize = RAF.readLong();
            // calculate real metadata position(42 + enc video size):
            long metadataOffset = HEADER_SIZE + encVideoSize;
            RAF.seek(metadataOffset); // seek to metadata block

            /*
            Задача прочитать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
             */

            // sizes block
            int totalMetadataSize = RAF.readInt();
            int encryptedJsonSize = RAF.readInt();
            int encryptedPreviewSize = RAF.readInt();

            // base nonce
            byte[] baseNonce = new byte[BASE_NONCE_LENGTH];
            readToByteArray(baseNonce);

            // read JSON and tag:
            byte[] ecryptedJson = new byte[encryptedJsonSize]; // use encryptedJsonSize to create buffer
            readToByteArray(ecryptedJson);
            byte[] jsonTag = new byte[TAG_SIZE];
            readToByteArray(jsonTag);

            // read preview and tag:
            byte[] ecryptedPreview = new byte[encryptedPreviewSize]; // use encryptedPreviewSize to create preview buffer
            readToByteArray(ecryptedPreview);
            byte[] previewTag = new byte[TAG_SIZE];
            readToByteArray(previewTag);

            // collecting results and create VideoMetadata class
            return new VideoMetadata(totalMetadataSize, encryptedJsonSize, encryptedPreviewSize, baseNonce, ecryptedJson, jsonTag, ecryptedPreview, previewTag);
        } catch (Exception e) {
            throw new PVVFException("Error to read pvvf metadata", e);
        }
    }

    /**
     * Собирает полный дескриптор видеофайла, включая заголовок и метаданные.
     * Данные видеопотока при этом не загружаются в память.
     *
     * @return Объект {@link EncryptedVideo}, готовый для использования.
     */
    public EncryptedVideo collectEncryptedVideo() {
        VideoHeader header = parseVideoHeader();
        return new EncryptedVideo(header, parseVideoMetadata());
    }

    /**
     * Читает зашифрованный чанк видео по его порядковому номеру.
     *
     * @param chunkIndex Индекс чанка.
     * @return Объект {@link VideoChunk}, содержащий зашифрованные данные и тег.
     */
    public synchronized VideoChunk parseVideoChunk(long chunkIndex) {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("Chunk index must be a greather then 0");
        }

        try {
            // step 0 - read mime type length for calculating offset.
            RAF.seek(6);
            byte mimeLength = RAF.readByte();

            // step 1 - calculating chunk offset in file:
            long chunkStart = HEADER_SIZE + mimeLength + (TOTAL_CHUNK_SIZE * chunkIndex);
            RAF.seek(chunkStart); // seek to chunk

            /*
            Задача прочитать чанк:
            1 - зашифрованный контент чанка
            2 - ChaCha20 тег чанка
             */

            byte[] chunkContent = new byte[PLAIN_CHUNK_SIZE]; // always 128kb
            readToByteArray(chunkContent);

            byte[] chunkTag = new byte[TAG_SIZE]; // always 16b
            readToByteArray(chunkTag);

            // collecting results:
            return new VideoChunk(chunkIndex, chunkContent, chunkTag);
        } catch (Exception e) {
            throw new PVVFException("Error to read pvvf chunk", e);
        }
    }

    public static boolean isPVVFFile(Path path) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            int magicNumberLength = MAGIC_NUMBER.length();
            ByteBuffer fileType = ByteBuffer.allocate(magicNumberLength);
            int bytesRead = channel.read(fileType);
            if (bytesRead != magicNumberLength) {
                log.warn("Incorrect bytes was readed: {}/{}", bytesRead, magicNumberLength);
                return false;
            }
            String magic = new String(fileType.array(), IO_CHARSET);
            return magic.equals(MAGIC_NUMBER);
        } catch (Exception e) {
            log.error("Error probe file type: ", e);
            return false;
        }
    }

    /**
     * Вспомогательный метод для чтения строки фиксированной длины.
     */
    @Contract("_ -> new")
    private @NonNull String readString(byte[] array) throws IOException {
        readToByteArray(array);
        return new String(array, IO_CHARSET);
    }

    /**
     * Вспомогательный метод для заполнения массива байт из текущей позиции RAF.
     * Выбрасывает исключение, если количество прочитанных байт не совпадает с длиной массива.
     */
    private void readToByteArray(byte[] array) throws IOException {
        int readed = RAF.read(array);
        if (readed != array.length) {
            throw new IOException("Invalid readed length: " + readed + "/" + array.length);
        }
    }

    /**
     * Закрывает {@link RandomAccessFile}.
     * Вызывается автоматически при использовании в try-with-resources.
     */
    @Override
    public void close() {
        try {
            RAF.close();
        } catch (IOException e) {
            throw new PVVFException("Error to close pvvf read stream", e);
        }
    }
}