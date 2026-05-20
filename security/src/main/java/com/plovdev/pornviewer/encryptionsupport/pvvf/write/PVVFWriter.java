package com.plovdev.pornviewer.encryptionsupport.pvvf.write;

import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoChunk;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader;
import com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoMetadata;
import com.plovdev.pornviewer.exceptions.PVVFException;
import com.plovdev.pornviewer.exceptions.PVVFOpenException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader.HEADER_SIZE;
import static com.plovdev.pornviewer.encryptionsupport.pvvf.videomodel.VideoHeader.MAGIC_NUMBER;

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
    private final DataOutputStream writeStream;

    /**
     * Создает экземпляр писателя для указанного файла.
     *
     * @param file Файл для записи.
     */
    public PVVFWriter(File file) {
        this.file = file;
        try {
            writeStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            throw new PVVFOpenException("Error to open pvvf write stream", e);
        }
    }

    public PVVFWriter(OutputStream stream) {
        writeStream = new DataOutputStream(new BufferedOutputStream(stream));
    }

    public File getFile() {
        return file;
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
        } catch (IOException e) {
            throw new PVVFException("Error to write video header", e);
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

            writeStream.flush();
        } catch (IOException e) {
            throw new PVVFException("Error towrite video metadata", e);
        }
    }

    @SuppressWarnings("resources")
    public synchronized void updateVideoMetadata(long encVideoSize, VideoMetadata toWrite) {
        Objects.requireNonNull(toWrite);
        if (encVideoSize < 0) {
            throw new IllegalArgumentException("Enc video size must be a greather then 0");
        }

        try (RandomAccessFile RAF = new RandomAccessFile(file, "rw")) {
            long metadataOffset = HEADER_SIZE + encVideoSize;
            RAF.seek(metadataOffset); // seek to metadata block

            /*
            Задача записать блок с метаданными:
            1 - размеры данных в метадате
            2 - metadata nonce
            3 - данные с их ChaCha20-tag'ами
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

            RAF.getChannel().truncate(RAF.getFilePointer()).force(true);
        } catch (IOException e) {
            throw new PVVFException("Error to update metadata", e);
        }
    }

    public synchronized void appendVideoChunk(VideoChunk videoChunk) {
        Objects.requireNonNull(videoChunk);

        try {
            writeStream.write(videoChunk.prepareChunk());
        } catch (IOException e) {
            throw new PVVFException("Error to append video chunk", e);
        }
    }

    /**
     * Вспомогательный метод для записи строки.
     */
    private void writeString(@NonNull String str) throws IOException {
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
     * Закрывает {@link RandomAccessFile}.
     * Вызывается автоматически при использовании в try-with-resources.
     */
    @Override
    public void close() {
        try {
            writeStream.close();
        } catch (IOException e) {
            throw new PVVFException("Error to close pvvf write stream", e);
        }
    }
}