package test.plovdev.pornviewer.services;

import com.plovdev.pornviewer.services.NumberUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberUtilsTest {
    @Test
    void testShortToBytes() {
        byte[] bytes = {0, 1};
        byte[] shortBytes = NumberUtils.shortToBytes((short) 1);
        assertArrayEquals(bytes, shortBytes);
    }

    @Test
    void testBytesToShort() {
        byte[] bytes = {0, 1};
        short value = NumberUtils.bytesToShort(bytes);
        assertEquals(1, value);
    }

    @Test
    void testIntToBytes() {
        byte[] bytes = {0, 0, 0, 1};
        byte[] intBytes = NumberUtils.intToBytes(1);
        assertArrayEquals(bytes, intBytes);
    }

    @Test
    void testBytesToInt() {
        byte[] bytes = {0, 0, 0, 1};
        int value = NumberUtils.bytesToInt(bytes);
        assertEquals(1, value);
    }

    @Test
    void testLongToBytes() {
        byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 1};
        byte[] longBytes = NumberUtils.longToBytes(1L);
        assertArrayEquals(bytes, longBytes);
    }

    @Test
    void testBytesToLong() {
        byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 1};
        long value = NumberUtils.bytesToLong(bytes);
        assertEquals(1L, value);
    }

    @Test
    void testFloatToBytes() {
        byte[] bytes = {63, -128, 0, 0}; // IEEE 754
        byte[] floatBytes = NumberUtils.floatToBytes(1.0f);
        assertArrayEquals(bytes, floatBytes);
    }

    @Test
    void testBytesToFloat() {
        byte[] bytes = {63, -128, 0, 0}; // IEEE 754
        float value = NumberUtils.bytesToFloat(bytes);
        assertEquals(1.0f, value);
    }
}