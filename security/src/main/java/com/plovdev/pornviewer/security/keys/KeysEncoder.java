package com.plovdev.pornviewer.security.keys;

public interface KeysEncoder {
    byte[] encode(char[] password);

    boolean verify(char[] password, byte[] encoded);
}