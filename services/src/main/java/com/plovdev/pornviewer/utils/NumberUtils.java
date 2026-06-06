package com.plovdev.pornviewer.utils;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class NumberUtils {
    /**
     * Преобразует short в массив из 2 байт (Big-Endian).
     */
    public static byte @NonNull [] shortToBytes(short value) {
        return ByteBuffer.allocate(2).putShort(value).array();
    }

    /**
     * Преобразует int в массив из 4 байт (Big-Endian).
     */
    public static byte @NonNull [] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * Преобразует long в массив из 8 байт (Big-Endian).
     */
    public static byte @NonNull [] longToBytes(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    /**
     * Преобразует float в массив из 4 байт (Big-Endian).
     */
    public static byte @NonNull [] floatToBytes(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    /**
     * Преобразует массив байт в short (Big-Endian).
     * Ожидает массив длиной до 2 байт.
     */
    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.allocate(2).put(bytes).getShort();
    }

    /**
     * Преобразует массив байт в int (Big-Endian).
     * Ожидает массив длиной до 4 байт.
     */
    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.allocate(4).put(bytes).getInt();
    }

    /**
     * Преобразует массив байт в float (Big-Endian).
     * Ожидает массив длиной до 4 байт.
     */
    public static float bytesToFloat(byte[] bytes) {
        return ByteBuffer.allocate(4).put(bytes).getFloat();
    }

    /**
     * Преобразует массив байт в long (Big-Endian).
     * Ожидает массив длиной до 8 байт.
     */
    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.allocate(8).put(bytes).getLong();
    }
}