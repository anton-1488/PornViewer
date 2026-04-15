package com.plovdev.pornviewer.encryptionsupport.videoparser.read;

import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk.PLAIN_CHUNK_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk.TOTAL_CHUNK_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader.*;
import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoMetadata.TAG_SIZE;

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
    private File file;
    private RandomAccessFile RAF;

    /**
     * Создает экземпляр парсера для указанного файла.
     *
     * @param file Файл в формате PVVF для анализа.
     */
    public PVVFParser(File file) {
        this.file = file;
        updateRaf(file);
    }

    public File getFile() {
        return file;
    }

    /**
     * Обновляет целевой файл и переоткрывает поток чтения.
     *
     * @param file Новый файл для парсинга.
     */
    public synchronized void setFile(File file) {
        this.file = file;
        updateRaf(file);
    }

    /**
     * Выполняет чтение и парсинг заголовка видеофайла (первые 42 байта).
     *
     * @return Объект {@link VideoHeader} с техническими параметрами видео.
     * @throws RuntimeException если структура заголовка нарушена или файл поврежден.
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

            // step 4 - read video mime type:
            String mimeType = readString(FOUR_BYTE_ARRAY);

            // step 5 - reading sizes:
            int lastChunkPaddingSize = RAF.readInt();
            long plainVideoSize = RAF.readLong();
            long encryptedVideoSize = RAF.readLong();

            // step 6 - read nonce and crc:
            byte[] baseNonce = new byte[BASE_NONCE_LENGTH];
            readToByteArray(baseNonce);
            int crc32 = RAF.readInt();

            // step 7 - check if file pointer is equal header size:
            if (RAF.getFilePointer() != HEADER_SIZE) {
                throw new IOException("Error parse file header: invalid pointer: " + RAF.getFilePointer());
            }

            // collecting results and return VideoHeader class
            VideoHeader header = new VideoHeader(fileVersion, flag, mimeType, lastChunkPaddingSize, plainVideoSize, encryptedVideoSize, baseNonce, crc32);

            int calculatedCrc = (int) header.calculateCRC32();
            // check the checksum
            if (crc32 != calculatedCrc) {
                log.warn("File: {}, getted crc: {}, calculated crc: {}", file.toString(), crc32, calculatedCrc);
                log.warn("Header CRC32 суммы не совпадают! RED FLAG, PORN ACCESS DENIED... System.exit(9)...");
            }

            return header;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Выполняет чтение метаданных, расположенных после зашифрованного тела видео.
     * Использует переданный заголовок для вычисления смещения блока метаданных.
     *
     * @param encVideoSize размер зашифрованного контента
     * @return Объект {@link VideoMetadata} или null, если заголовок отсутствует.
     */
    public synchronized VideoMetadata parseVideoMetadata(long encVideoSize) {
        if (encVideoSize < 0) {
            throw new IllegalArgumentException("Enc video size must be a greather then 0");
        }

        try {
            // calculate real metadata position(42 + enc video size):
            long metadataOffset = HEADER_SIZE + encVideoSize;
            RAF.seek(metadataOffset); // seek to metadata block

            /*
            Задача прочитать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
            4 - crc32 и собрать данные в VideoMetadata
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

            int crc32 = RAF.readInt();

            // check if file pointer at end:
            if (RAF.getFilePointer() != file.length()) {
                throw new IOException("Error parse video metadata: invalid pointer: " + RAF.getFilePointer());
            }

            // collecting results and create VideoMetadata class
            VideoMetadata metadata = new VideoMetadata(totalMetadataSize, encryptedJsonSize, encryptedPreviewSize, baseNonce, ecryptedJson, jsonTag, ecryptedPreview, previewTag, crc32);

            // check the checksum
            int calculatedCrc = (int) metadata.calculateCRC32();
            if (crc32 != calculatedCrc) {
                log.warn("Getted heaser crc: {}, calculated crc: {}", crc32, calculatedCrc);
                log.warn("Metadata CRC32 суммы не совпадают! RED FLAG, PORN ACCESS DENIED... System.exit(9)...");
            }

            return metadata;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        return new EncryptedVideo(header, parseVideoMetadata(header.encVideoSize()));
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
            // step 1 - calculating chunk offset in file:
            long chunkStart = HEADER_SIZE + (TOTAL_CHUNK_SIZE * chunkIndex);
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
            throw new RuntimeException(e);
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
    private String readString(byte[] array) throws IOException {
        readToByteArray(array);
        return new String(array, IO_CHARSET);
    }

    /**
     * Вспомогательный метод для заполнения массива байт из текущей позиции RAF.
     * Выбрасывает исключение, если количество прочитанных байт не совпадает с длиной массива.
     */
    private void readToByteArray(byte[] array) throws IOException {
        int bytesRead;
        int offset = 0;

        while (offset < array.length && (bytesRead = RAF.read(array, offset, array.length - offset)) != -1) {
            offset += bytesRead;
        }
    }

    /**
     * Инициализирует или переоткрывает {@link RandomAccessFile} в режиме чтения.
     *
     * @param file Файл для открытия.
     * @throws RuntimeException если файл не найден или доступ запрещен.
     */
    private void updateRaf(File file) {
        try {
            if (RAF != null) {
                RAF.close();
            }
            RAF = new RandomAccessFile(file, "r");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Закрывает {@link RandomAccessFile}.
     * Вызывается автоматически при использовании в try-with-resources.
     */
    @Override
    public void close() throws Exception {
        RAF.close();
    }
}