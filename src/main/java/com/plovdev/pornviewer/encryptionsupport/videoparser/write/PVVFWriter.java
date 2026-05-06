package com.plovdev.pornviewer.encryptionsupport.videoparser.write;

import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader.HEADER_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.videoparser.videomodel.VideoHeader.MAGIC_NUMBER;

public class PVVFWriter implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PVVFWriter.class);

    /**
     * Charset to string decoding
     */
    private static final Charset IO_CHARSET = StandardCharsets.US_ASCII;

    /**
     * Sources to write data.
     */
    private File file;
    private DataOutputStream writeStream;

    /**
     * Создает экземпляр писателя для указанного файла.
     *
     * @param file Файл для записи.
     */
    public PVVFWriter(File file) {
        this.file = file;
        updateStream(file);
    }

    public PVVFWriter(OutputStream stream) {
        writeStream = new DataOutputStream(new BufferedOutputStream(stream));
    }
    public PVVFWriter(BufferedOutputStream stream) {
        writeStream = new DataOutputStream(stream);
    }

    public File getFile() {
        return file;
    }

    /**
     * Обновляет целевой файл и переоткрывает поток записи.
     *
     * @param file Новый файл для записи.
     */
    public synchronized void setFile(File file) {
        this.file = file;
        updateStream(file);
    }


    public synchronized void writeVideoHeader(VideoHeader videoHeader) {
        Objects.requireNonNull(videoHeader);

        try {
            // step 1 - wrie magic number:
            writeString(MAGIC_NUMBER);

            // step 2 - write version:
            writeStream.writeByte(videoHeader.version());
            // step 3 - write flag:
            writeStream.writeByte(videoHeader.flag());

            // step 4 - write video mime type:
            writeString(videoHeader.mime());


            // step 5 - write sizes:
            writeStream.writeInt(videoHeader.lastChunkPaddingSize());
            writeStream.writeLong(videoHeader.plainVideoSize());
            writeStream.writeLong(videoHeader.encVideoSize());

            // step 6 - write nonce and crc:
            writeStream.write(videoHeader.baseNonce());
            writeStream.writeInt((int) videoHeader.headerCRC32());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeVideoMetadata(VideoMetadata toWrite) {
        Objects.requireNonNull(toWrite);

        try {
            /*
            Задача записать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
            4 - crc32
             */

            // sizes block
            writeStream.writeInt(toWrite.metadataSize());
            writeStream.writeInt(toWrite.encryptedJsonSize());
            writeStream.writeInt(toWrite.encryptedPreviewSize());

            // base nonce
            writeStream.write(toWrite.metadataNonce());

            // write JSON and tag:
            writeStream.write(toWrite.encryptedJson());
            writeStream.write(toWrite.jsonTag());

            // read preview and tag:
            writeStream.write(toWrite.encryptedPreview());
            writeStream.write(toWrite.previewTag());

            writeStream.writeInt((int) toWrite.calculateCRC32());

            writeStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resources")
    public synchronized void updateVideoMetadata(long encVideoSize, VideoMetadata toWrite) {
        Objects.requireNonNull(toWrite);
        if (encVideoSize < 0) {
            throw new IllegalArgumentException("Enc video size must be a greather then 0");
        }

        try (RandomAccessFile RAF = new RandomAccessFile(file, "rw")) {
            // calculate real metadata position(42 + enc video size):
            long metadataOffset = HEADER_SIZE + encVideoSize;
            RAF.seek(metadataOffset); // seek to metadata block

            /*
            Задача записать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
            4 - crc32
             */

            // sizes block
            RAF.writeInt(toWrite.metadataSize());
            RAF.writeInt(toWrite.encryptedJsonSize());
            RAF.writeInt(toWrite.encryptedPreviewSize());

            // base nonce
            RAF.write(toWrite.metadataNonce());

            // write JSON and tag:
            RAF.write(toWrite.encryptedJson());
            RAF.write(toWrite.jsonTag());

            // read preview and tag:
            RAF.write(toWrite.encryptedPreview());
            RAF.write(toWrite.previewTag());

            RAF.writeInt((int) toWrite.calculateCRC32());

            RAF.getChannel().truncate(RAF.getFilePointer()).force(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void appendVideoChunk(VideoChunk videoChunk) {
        Objects.requireNonNull(videoChunk);

        try {
            writeStream.write(videoChunk.prepareChunk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод для записи строки.
     */
    private void writeString(String str) throws IOException {
        byte[] bytes = str.getBytes(IO_CHARSET);
        if (bytes.length != 4) {
            byte[] fixed = new byte[4];
            System.arraycopy(bytes, 0, fixed, 0, Math.min(bytes.length, 4));
            writeStream.write(fixed);
        } else {
            writeStream.write(bytes);
        }
    }


    /**
     * Инициализирует или переоткрывает поток для записи.
     *
     * @param file Файл для открытия.
     * @throws RuntimeException если файл не найден или доступ запрещен.
     */
    private void updateStream(File file) {
        try {
            if (writeStream != null) {
                writeStream.close();
            }
            writeStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
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
        writeStream.close();
    }
}