package com.plovdev.pornviewer.encryptionsupport.pvvf.read;

import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.EncryptedVideo;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoMetadata;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk.PLAIN_CHUNK_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk.TOTAL_CHUNK_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader.*;
import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoMetadata.TAG_SIZE;

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
    private final Path file;
    private final long fileSize;
    private final Arena fileArena = Arena.ofConfined();
    private final MemorySegment mappedFile;

    /**
     * Создает экземпляр парсера для указанного файла.
     *
     * @param file Файл в формате PVVF для анализа.
     */
    public PVVFParser(Path file) {
        this.file = file;
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            this.fileSize = channel.size();
            if (fileSize < 42) {
                throw new IllegalArgumentException("File size is incrorrect");
            }
            this.mappedFile = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize, fileArena);
        } catch (Exception e) {
            fileArena.close();
            throw new RuntimeException("Failed to memory-map file: " + file, e);
        }
    }

    public Path getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }

    /**
     * Выполняет чтение и парсинг заголовка видеофайла (первые 42 байта).
     *
     * @return Объект {@link VideoHeader} с техническими параметрами видео.
     * @throws RuntimeException если структура заголовка нарушена или файл поврежден.
     */
    public synchronized VideoHeader parseVideoHeader() {
        long offset = 0;
        try {
            // step 1 - check magic number:
            String readedMagic = readString(offset, 4); // 4 = "PVVF".length()
            offset += 4;
            if (!readedMagic.equals(MAGIC_NUMBER)) {
                throw new IOException("Illegal magic number in file: " + readedMagic);
            }

            // step 2 - read version:
            byte fileVersion = mappedFile.get(ValueLayout.JAVA_BYTE, offset);
            offset++;
            // step 3 - read flag:
            byte flag = mappedFile.get(ValueLayout.JAVA_BYTE, offset);
            offset++;

            // step 4 - read video mime type:
            String mimeType = readString(offset, 4); // MIME type always 4 bytes(MP4 , WEBM, etc.)
            offset += 4;

            // step 5 - reading sizes:
            int lastChunkPaddingSize = mappedFile.get(ValueLayout.JAVA_INT_UNALIGNED, offset);
            offset += 4;

            long plainVideoSize = mappedFile.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
            offset += 8;

            long encryptedVideoSize = mappedFile.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
            offset += 8;

            // step 6 - read nonce and crc:
            byte[] baseNonce = readBytes(offset, BASE_NONCE_LENGTH);
            offset += BASE_NONCE_LENGTH;

            int crc32 = mappedFile.get(ValueLayout.JAVA_INT_UNALIGNED, offset);

            // collecting results and return VideoHeader class
            VideoHeader header = new VideoHeader(fileVersion, flag, mimeType, lastChunkPaddingSize, plainVideoSize, encryptedVideoSize, baseNonce, crc32);

            // check the checksum
            if (crc32 != (int) header.calculateCRC32()) {
                log.warn("Getted crc: {}, calculated crc: {}", crc32, header.calculateCRC32());
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
            long offset = HEADER_SIZE + encVideoSize;

            /*
            Задача прочитать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
            4 - crc32 и собрать данные в VideoMetadata
             */

            // sizes block
            int totalMetadataSize = mappedFile.get(ValueLayout.JAVA_INT, offset);
            offset += 4;

            int encryptedJsonSize = mappedFile.get(ValueLayout.JAVA_INT, offset);
            offset += 4;

            int encryptedPreviewSize = mappedFile.get(ValueLayout.JAVA_INT, offset);
            offset += 4;

            // base nonce
            byte[] baseNonce = readBytes(offset, BASE_NONCE_LENGTH);
            offset += BASE_NONCE_LENGTH;

            // read JSON and tag:
            byte[] encryptedJson = readBytes(offset, encryptedJsonSize);
            offset += encryptedJsonSize;
            byte[] jsonTag = readBytes(offset, TAG_SIZE);
            offset += TAG_SIZE;

            // read preview and tag:
            byte[] encryptedPreview = readBytes(offset, encryptedPreviewSize);
            offset += encryptedPreviewSize;
            byte[] previewTag = readBytes(offset, TAG_SIZE);
            offset += TAG_SIZE;

            int crc32 = mappedFile.get(ValueLayout.JAVA_INT, offset);
            offset += 4;

            // check if file pointer at end:
            if (offset != fileSize) {
                throw new IOException("Error parse video metadata: invalid pointer: " + offset);
            }

            // collecting results and create VideoMetadata class
            VideoMetadata metadata = new VideoMetadata(totalMetadataSize, encryptedJsonSize, encryptedPreviewSize, baseNonce, encryptedJson, jsonTag, encryptedPreview, previewTag, crc32);

            // check the checksum
            if (crc32 != (int) metadata.calculateCRC32()) {
                log.warn("Getted heaser crc: {}, calculated crc: {}", crc32, metadata.calculateCRC32());
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

            /*
            Задача прочитать чанк:
            1 - зашифрованный контент чанка
            2 - ChaCha20 тег чанка
             */

            byte[] chunkContent = readBytes(chunkStart, PLAIN_CHUNK_SIZE);
            chunkStart += PLAIN_CHUNK_SIZE;

            byte[] chunkTag = readBytes(chunkStart, TAG_SIZE);

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
    @Contract("_, _ -> new")
    private @NotNull String readString(long offset, int length) {
        return new String(readBytes(offset, length), IO_CHARSET);
    }

    /**
     * Вспомогательный метод для заполнения массива байт из текущей позиции RAF.
     * Выбрасывает исключение, если количество прочитанных байт не совпадает с длиной массива.
     */
    private byte @NotNull [] readBytes(long offset, int length) {
        MemorySegment slice = mappedFile.asSlice(offset, length);
        return slice.toArray(ValueLayout.JAVA_BYTE);
    }

    /**
     * Закрывает file arena.
     * Вызывается автоматически при использовании в try-with-resources.
     */
    @Override
    public void close() {
        fileArena.close();
    }
}