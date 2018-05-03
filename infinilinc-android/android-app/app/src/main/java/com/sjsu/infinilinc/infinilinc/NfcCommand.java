package com.sjsu.infinilinc.infinilinc;

import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sjsu.infinilinc.infinilinc.NfcCommand.TYPE.READ;
import static com.sjsu.infinilinc.infinilinc.NfcCommand.TYPE.SELECT_AID;
import static com.sjsu.infinilinc.infinilinc.NfcCommand.TYPE.SET_DATA_SIZE;
import static com.sjsu.infinilinc.infinilinc.NfcCommand.TYPE.SEND;
import static com.sjsu.infinilinc.infinilinc.NfcCommand.TYPE.INVALID;

class NfcCommand {
    private static byte[] AID               = {(byte)0xF6, (byte)0x62, (byte)0x58, (byte)0x1E,
                                               (byte)0x83, (byte)0x64, (byte)0xCA, (byte)0xDF,
                                               (byte)0x67, (byte)0x49, (byte)0x6A, (byte)0x2B,
                                               (byte)0x16, (byte)0xD8, (byte)0x4B, (byte)0x5F};

    private static byte[] C_SELECT          = {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00,
                                               (byte)0x10};
    private static byte[] C_TX              = {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00};
    private static byte[] C_RX              = {(byte)0x80, (byte)0x01, (byte)0x00, (byte)0x00};
    private static byte[] C_SET_DATA_SIZE   = {(byte)0x80, (byte)0x02, (byte)0x00, (byte)0x00};

    enum TYPE {
        INVALID,
        SELECT_AID,
        SEND,
        SET_DATA_SIZE,
        READ
    }

    private TYPE type;
    private int dataSize;
    private byte[] payload;
    private byte[] responsePayload;
    private boolean moreFragments;

    NfcCommand(TYPE t) {
        dataSize = 0;
        payload = null;
        responsePayload = null;
        moreFragments = false;

        type = t;
    }

    NfcCommand(byte[] bytes) {
        dataSize = 0;
        payload = null;
        responsePayload = null;
        moreFragments = false;

        type = parse(bytes);
    }

    private TYPE parse(byte[] bytes) {
        if(bytes.length < 4) {
            return INVALID;
        }

        switch(bytes[0]) {
            case (byte)0x00: return parseCmdSelect(bytes);
            case (byte)0x80: return parseCmd(bytes);
            default:         return INVALID;
        }
    }

    private TYPE parseCmdSelect(byte[] bytes) {
        if(Arrays.equals(bytes, getCmdSelectAid())) {
            return SELECT_AID;
        }

        return INVALID;
    }

    private TYPE parseCmd(byte[] bytes) {
        if(bytes[1] == C_SET_DATA_SIZE[1]) {
            return parseCmdSetDataSize(bytes);
        }

        switch(bytes[1]) {
            case (byte)0x00: return parseCmdTx(bytes);
            case (byte)0x01: return parseCmdRx(bytes);
            case (byte)0x02: return parseCmdSetDataSize(bytes);
            default:         return INVALID;
        }
    }

    private TYPE parseCmdTx(byte[] bytes) {
        if(bytes.length < 4) {
            return INVALID;
        }

        payload = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, payload, 0, bytes.length - 4);

        switch(bytes[2]) {
            case (byte)0x00:
                moreFragments = false;
                return SEND;

            case (byte)0x01:
                moreFragments = true;
                return SEND;

            default:
                return INVALID;
        }
    }

    private TYPE parseCmdRx(byte[] bytes) {
        if(Arrays.equals(bytes, C_RX)) {
            return READ;
        }

        return INVALID;
    }

    private TYPE parseCmdSetDataSize(byte[] bytes) {
        if(bytes.length < 4) {
            return INVALID;
        }

        dataSize = bytes[2] & 0xFF;

        return SET_DATA_SIZE;
    }

    String getPayloadString() {
        return new String(payload);
    }

    byte[] getPayloadBytes() {
        return payload;
    }

    void setPayload(byte[] bytes) {
        payload = bytes;
    }

    void setPayload(String str) {
        payload = new byte[str.length()];

        for(int i = 0; i < str.length(); i++) {
            payload[i] = (byte)str.charAt(i);
        }
    }

    byte[] getResponsePayloadBytes() {
        return responsePayload;
    }

    String getResponsePayloadString() {
        return (responsePayload != null) ? new String(responsePayload) : null;
    }

    TYPE getType() {
        return type;
    }

    private static byte[] getCmdSelectAid() {
        byte[] c = new byte[C_SELECT.length + AID.length];
        System.arraycopy(C_SELECT, 0, c, 0, C_SELECT.length);
        System.arraycopy(AID, 0, c, C_SELECT.length, AID.length);

        return c;
    }

    int getDataSize() {
        return dataSize;
    }

    boolean setDataSize(int size) {
        if((size < 0) || (size > 0xFFFF)) {
            return false;
        }

        dataSize = size;

        return true;
    }

    boolean send(IsoDep tag) {
        switch(type) {
            case SEND:          return sendCmdTx(tag);
            case READ:          return sendCmdRx(tag);
            case SET_DATA_SIZE: return sendCmdSetDataSize(tag);
            case SELECT_AID:    return sendCmdSelectAid(tag);
            default:            return false;
        }
    }

    private boolean sendCmdSelectAid(IsoDep tag) {
        byte[] res_bytes;

        try {
            res_bytes = tag.transceive(getCmdSelectAid());
        } catch(IOException e) {
            return false;
        }

        if((new NfcResponse(res_bytes)).getType() != NfcResponse.TYPE.OK) {
            return false;
        }

        return true;
    }

    private boolean sendCmdTx(IsoDep tag) {
        if((dataSize == 0) || (payload == null)) {
            return false;
        }

        byte[] cmd;
        byte[] res;
        int i;

        List<byte[]> frags = NfcHelper.fragment(payload, dataSize - 4);

        /* Send all but the last data fragment */
        for(i = 0; i < (frags.size() - 1); i++) {
            cmd = new byte[frags.get(i).length + 4];

            /* Add the command before the payload */
            System.arraycopy(C_TX, 0, cmd, 0, 4);
            cmd[2] = (byte)0x01;

            System.arraycopy(frags.get(i), 0, cmd, 4, frags.get(i).length);

            try {
                res = tag.transceive(cmd);
            } catch(IOException e) {
                return false;
            }

            if((new NfcResponse(res)).getType() != NfcResponse.TYPE.OK) {
                return false;
            }
        }

        /* Send the last data fragment */

        cmd = new byte[frags.get(i).length + 4];

        System.arraycopy(C_TX, 0, cmd, 0, 4);
        cmd[2] = (byte)0x00;

        System.arraycopy(frags.get(i), 0, cmd, 4, frags.get(i).length);

        try {
            res = tag.transceive(cmd);
        } catch(IOException e) {
            return false;
        }

        if((new NfcResponse(res)).getType() != NfcResponse.TYPE.OK) {
            return false;
        }

        return true;
    }

    private boolean sendCmdRx(IsoDep tag) {
        if(dataSize == 0) {
            return false;
        }

        byte[] res_bytes;
        NfcResponse res;

        List<byte[]> payloadFrags = new ArrayList<>();

        /* Read from the card until no more data is available */
        while(true) {
            try {
                res_bytes = tag.transceive(C_RX);
            } catch(IOException e) {
                return false;
            }

            res = new NfcResponse(res_bytes);

            /* Save payload fragment */
            payloadFrags.add(res.getPayloadBytes());

            if(res.getType() != NfcResponse.TYPE.OK) {
                return false;
            }

            if(!res.more()) {
                break;
            }
        }

        int payloadLength = 0;

        /* Get the total payload length */
        for(int i = 0; i < payloadFrags.size(); i++) {
            payloadLength += payloadFrags.get(i).length;
        }

        responsePayload = new byte[payloadLength];

        int offset = 0;

        /* Concatenate the payload fragments to the payload buffer */
        for(int i = 0; i < payloadFrags.size(); i++) {
            System.arraycopy(payloadFrags.get(i), 0, responsePayload, offset,
                    payloadFrags.get(i).length);

            offset += payloadFrags.get(i).length;
        }

        return true;
    }

    private boolean sendCmdSetDataSize(IsoDep tag) {
        if(dataSize == 0) {
            return false;
        }

        byte[] cmd = new byte[C_SET_DATA_SIZE.length];

        System.arraycopy(C_SET_DATA_SIZE, 0, cmd, 0, 2);

        /* Little-endian order for data size */
        cmd[2] = (byte)(dataSize & 0xFF);
        cmd[3] = (byte)((dataSize >> 8) & 0xFF);

        byte[] res_bytes;

        try {
            res_bytes = tag.transceive(cmd);
        } catch(IOException e) {
            return false;
        }

        if((new NfcResponse(res_bytes)).getType() != NfcResponse.TYPE.OK) {
            return false;
        }

        return true;
    }

    boolean more() {
        return moreFragments;
    }

    void setMore(boolean m) {
        moreFragments = m;
    }
}
