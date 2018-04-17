package com.sjsu.infinilinc.infinilinc;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NfcResponseTest {
    private byte[] testPayload = {
            (byte)0x11, (byte)0xFF, (byte)0x22, (byte)0xEE,
            (byte)0x33, (byte)0xDD, (byte)0x44, (byte)0xCC
    };

    @Test
    public void parse_Empty() {
        byte[] input = {};

        NfcResponse response = new NfcResponse(input);

        /* Expect an invalid type */

        assertEquals(NfcResponse.TYPE.INVALID, response.getType());
    }

    @Test
    public void parse_TooShort() {
        byte[] input = {(byte)0x90};

        NfcResponse response = new NfcResponse(input);

        /* Expect an invalid type */

        assertEquals(NfcResponse.TYPE.INVALID, response.getType());
    }

    @Test
    public void parse_TypeInvalid() {
        byte[] input = {(byte)0x00, (byte)0x00};

        NfcResponse response = new NfcResponse(input);

        /* Expect an invalid type */

        assertEquals(NfcResponse.TYPE.INVALID, response.getType());
    }

    @Test
    public void parse_Ok_NoMore_NoPayload() {
        byte[] input = {(byte)0x90, (byte)0x00};

        NfcResponse response = new NfcResponse(input);

        /* Expect OK type, more = false, empty payload */

        assertEquals(NfcResponse.TYPE.OK, response.getType());
        assertEquals(false, response.more());
        assertArrayEquals(new byte[] {}, response.getPayloadBytes());
    }

    @Test
    public void parse_Ok_NoMore_Payload() {
        byte[] input = {(byte)0x90, (byte)0x00};

        input = concat(input, testPayload);

        NfcResponse response = new NfcResponse(input);

        /* Expect OK type, more = false, payload = testPayload */

        assertEquals(NfcResponse.TYPE.OK, response.getType());
        assertEquals(false, response.more());
        assertArrayEquals(testPayload, response.getPayloadBytes());
    }

    @Test
    public void parse_Ok_More_NoPayload() {
        byte[] input = {(byte)0x90, (byte)0x01};

        NfcResponse response = new NfcResponse(input);

        /* Expect OK type, more = true, empty payload */

        assertEquals(NfcResponse.TYPE.OK, response.getType());
        assertEquals(true, response.more());
        assertArrayEquals(new byte[] {}, response.getPayloadBytes());
    }

    @Test
    public void parse_Ok_More_Payload() {
        byte[] input = {(byte)0x90, (byte)0x01};

        input = concat(input, testPayload);

        NfcResponse response = new NfcResponse(input);

        /* Expect OK type, more = true, payload = testPayload */

        assertEquals(NfcResponse.TYPE.OK, response.getType());
        assertEquals(true, response.more());
        assertArrayEquals(testPayload, response.getPayloadBytes());
    }

    @Test
    public void parse_Ok_InvalidMore() {
        byte[] input = {(byte)0x90, (byte)0xFF};

        NfcResponse response = new NfcResponse(input);

        /* Expect an invalid type */

        assertEquals(NfcResponse.TYPE.INVALID, response.getType());
    }

    @Test
    public void parse_Err() {
        byte[] input = {(byte)0x6F, (byte)0x55};

        NfcResponse response = new NfcResponse(input);

        /* Expect error type, error code of 0x55 */

        assertEquals(NfcResponse.TYPE.ERROR, response.getType());
        assertEquals(input[1], response.getErrorCode());
    }

    @Test
    public void generate_Invalid() {}

    @Test
    public void generate_Ok_NoMore_NoPayload() {}

    @Test
    public void generate_Ok_NoMore_Payload() {}

    @Test
    public void generate_Ok_More_NoPayload() {}

    @Test
    public void generate_Ok_More_Payload() {}

    @Test
    public void generate_Err_NoCodeSet() {}

    @Test
    public void generate_Err_CodeSet() {}

    private byte[] concat(byte[] a, byte[] b) {
        if((a == null) || (b == null)) {
            return null;
        }

        byte[] res = new byte[a.length + b.length];

        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);

        return res;
    }
}
