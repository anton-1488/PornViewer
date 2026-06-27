package com.plovdev.pornviewer.services.crypto;

import java.util.HexFormat;

public final class HexUtils {
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public static String ofHex(byte[] hex) {
        return HEX_FORMAT.formatHex(hex);
    }

    public static byte[] toHex(String hexStr) {
        return HEX_FORMAT.parseHex(hexStr);
    }
}