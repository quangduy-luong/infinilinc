package com.sjsu.infinilinc.infinilinc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class NfcHelperTest {
    byte[] bytes_00 = {};
    byte[] bytes_32 = {
        (byte)0x7c, (byte)0xba, (byte)0xe7, (byte)0x5a,
        (byte)0x84, (byte)0xdd, (byte)0xf9, (byte)0x89,
        (byte)0x50, (byte)0xdd, (byte)0x2d, (byte)0x51,
        (byte)0x2a, (byte)0xcf, (byte)0xa6, (byte)0xad,
        (byte)0xc0, (byte)0xfd, (byte)0x67, (byte)0x09,
        (byte)0x2b, (byte)0xb0, (byte)0x83, (byte)0xd5,
        (byte)0x0c, (byte)0xbe, (byte)0x17, (byte)0x05,
        (byte)0x4a, (byte)0xdc, (byte)0x72, (byte)0x0c
    };

    @Test(expected = RuntimeException.class)
    public void fragmentUndefinedTest() {
        List<byte[]> res;

        /* Case 0: Array length > 0, fragment size = 0 */

        res = NfcHelper.fragment(bytes_32, 0);
    }

    @Test
    public void fragmentTest() {
        List<byte[]> res, expected;

        /* Case 0: Array length = 0, fragment size = 0 */

        expected = new ArrayList<>();
        expected.add(new byte[] {});

        res = NfcHelper.fragment(bytes_00, 0);

        assertArrayEquals(res.get(0), expected.get(0));

        /* Case 1: Array length > 0, fragment size > 0, fragment size is factor of array length */

        expected = new ArrayList<>();
        expected.add(Arrays.copyOfRange(bytes_32, 0, 8));
        expected.add(Arrays.copyOfRange(bytes_32, 8, 16));
        expected.add(Arrays.copyOfRange(bytes_32, 16, 24));
        expected.add(Arrays.copyOfRange(bytes_32, 24, 32));

        res = NfcHelper.fragment(bytes_32, 8);

        assertArrayEquals(res.get(0), expected.get(0));
        assertArrayEquals(res.get(1), expected.get(1));
        assertArrayEquals(res.get(2), expected.get(2));
        assertArrayEquals(res.get(3), expected.get(3));

        /* Case 2: Array length > 0, fragment size > 0, fragment size is not factor of array length */

        expected = new ArrayList<>();
        expected.add(Arrays.copyOfRange(bytes_32, 0, 7));
        expected.add(Arrays.copyOfRange(bytes_32, 7, 14));
        expected.add(Arrays.copyOfRange(bytes_32, 14, 21));
        expected.add(Arrays.copyOfRange(bytes_32, 21, 28));
        expected.add(Arrays.copyOfRange(bytes_32, 28, 32));

        res = NfcHelper.fragment(bytes_32, 7);

        assertArrayEquals(res.get(0), expected.get(0));
        assertArrayEquals(res.get(1), expected.get(1));
        assertArrayEquals(res.get(2), expected.get(2));
        assertArrayEquals(res.get(3), expected.get(3));
        assertArrayEquals(res.get(4), expected.get(4));

        /* Case 3: Array length > 0, fragment size > 0, fragment size = array length */

        expected = new ArrayList<>();
        expected.add(Arrays.copyOfRange(bytes_32, 0, 32));

        res = NfcHelper.fragment(bytes_32, 32);

        assertArrayEquals(res.get(0), expected.get(0));

        /* Case 4: Array length = 0, fragment size > 0 */

        expected = new ArrayList<>();
        expected.add(new byte[] {});

        res = NfcHelper.fragment(bytes_00, 32);

        assertArrayEquals(res.get(0), expected.get(0));
    }
}
