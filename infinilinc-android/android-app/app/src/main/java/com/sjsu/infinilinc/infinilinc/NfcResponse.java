package com.sjsu.infinilinc.infinilinc;

import static com.sjsu.infinilinc.infinilinc.NfcResponse.TYPE.ERROR;
import static com.sjsu.infinilinc.infinilinc.NfcResponse.TYPE.INVALID;
import static com.sjsu.infinilinc.infinilinc.NfcResponse.TYPE.OK;

public class NfcResponse {
    enum TYPE {
        INVALID,
        OK,
        ERROR
    }

    private TYPE type;
    private byte errorCode;
    private byte[] payload;
    private boolean moreFragments;

    NfcResponse(TYPE t) {
        errorCode = 0;
        payload = null;
        moreFragments = false;

        type = t;
    }

    NfcResponse(byte[] bytes) {
        errorCode = 0;
        payload = null;

        type = parse(bytes);
    }

    private TYPE parse(byte[] bytes) {
        if(bytes.length < 2) {
            return INVALID;
        }

        switch(bytes[0]) {
            case (byte)0x90: return parseSuccess(bytes);
            case (byte)0x6F: return parseError(bytes);
            default:         return INVALID;
        }
    }

    private TYPE parseSuccess(byte[] bytes) {
        if(bytes.length > 2) {
            payload = new byte[bytes.length - 2];
            System.arraycopy(bytes, 2, payload, 0, bytes.length - 2);
        }

        switch(bytes[1]) {
            case (byte)0x00:
                return OK;

            case (byte)0x01:
                moreFragments = true;
                return OK;

            default:
                return INVALID;
        }
    }

    private TYPE parseError(byte[] bytes) {
        errorCode = bytes[1];

        return ERROR;
    }

    boolean more() {
        return moreFragments;
    }

    void setMore(boolean m) {
        moreFragments = m;
    }

    TYPE getType() {
        return type;
    }

    byte getErrorCode() {
        return errorCode;
    }

    void setErrorCode(byte code) {
        errorCode = code;
    }

    byte[] getPayloadBytes() {
        return payload;
    }

    String getPayloadString() {
        return (payload != null) ? new String(payload) : null;
    }

    void setPayload(byte[] bytes) {
        payload = bytes;
    }

    void setPayload(String str) {
        payload = str.getBytes();
    }

    byte[] getBytes() {
        byte[] res;

        if(payload != null) {
            res = new byte[payload.length + 2];
        } else {
            res = new byte[2];
        }

        switch(type) {
            case OK:
                res[0] = (byte)0x90;
                res[1] = (moreFragments) ? (byte)0x01 : (byte)0x00;

                if(payload != null) {
                    System.arraycopy(payload, 0, res, 2, payload.length);
                }

                return res;

            case ERROR:
                res[0] = 0x6F;
                res[1] = 0x00;

                return res;

            default: return null;
        }
    }
}
