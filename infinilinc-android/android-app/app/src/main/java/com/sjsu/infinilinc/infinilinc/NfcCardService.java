package com.sjsu.infinilinc.infinilinc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

public class NfcCardService extends HostApduService {
    static final String ACTION_CONNECT = "ACTION_CONNECT";
    static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";
    static final String ACTION_RECEIVED = "ACTION_RECEIVED";
    static final String ACTION_SEND = "ACTION_SEND";

    private String rxBuffer;
    private String txBuffer;

    private List<byte[]> txBufferFrags = null;
    private int fragNum;

    private int fragmentSize = 0;

    private boolean pendingRead = false;
    private boolean txBufferAvailable = false;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Configure the receiver intent filter */
        IntentFilter receiverIntentFilter = new IntentFilter();
        receiverIntentFilter.addAction(ACTION_SEND);

        /* Install the broadcast receiver */
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if((action != null) && (action.equals(ACTION_SEND))) {
                    txBuffer = intent.getStringExtra(Intent.EXTRA_TEXT);
                    txBufferFrags = null; /* New string: clear the fragment buffer */
                    txBufferAvailable = true;

                    if(pendingRead) {
                        /* Send a fragment if a read operation is pending */
                        pendingRead = false;
                        sendResponseApdu(getNextTxFragment());
                    }
                }
            }
        };

        LocalBroadcastManager l = LocalBroadcastManager.getInstance(getApplicationContext());
        l.registerReceiver(receiver, receiverIntentFilter);
    }

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(getApplicationContext());

        Intent intent;

        NfcCommand cmd = new NfcCommand(bytes);

        switch(cmd.getType()) {
            case SELECT_AID:
                /* Notify app that we've connected */
                intent = new Intent(ACTION_CONNECT);
                l.sendBroadcast(intent);

                rxBuffer = "";

                return new NfcResponse(NfcResponse.TYPE.OK).getBytes();

            case SEND:
                rxBuffer += cmd.getPayloadString();

                if(!cmd.more()) {
                    /* Send the data buffer to the app */
                    intent = new Intent(ACTION_RECEIVED);
                    intent.putExtra(Intent.EXTRA_TEXT, rxBuffer);
                    l.sendBroadcast(intent);
                }

                rxBuffer = "";

                return new NfcResponse(NfcResponse.TYPE.OK).getBytes();

            case SET_DATA_SIZE:
                fragmentSize = cmd.getDataSize();

                return new NfcResponse(NfcResponse.TYPE.OK).getBytes();

            case READ:
                if(!txBufferAvailable) {
                    /* We'll send the response once a string has been provided by the activity */
                    pendingRead = true;
                    return null;
                }

                return getNextTxFragment();

            default:
                return new NfcResponse(NfcResponse.TYPE.ERROR).getBytes();
        }
    }

    @Override
    public void onDeactivated(int i) {
        Context c = getApplicationContext();
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(c);

        Intent intent = new Intent(ACTION_DISCONNECT);

        l.sendBroadcast(intent);
    }

    private byte[] getNextTxFragment() {
        if(fragmentSize == 0) {
            /* Fragment size hasn't been set -- error */
            return new NfcResponse(NfcResponse.TYPE.ERROR).getBytes();
        }

        NfcResponse res;

        /* Fragment the output buffer if not done already */
        if(txBufferFrags == null) {
            txBufferFrags = NfcHelper.fragment(txBuffer.getBytes(), fragmentSize - 2);
            fragNum = 0;
        }

        if(fragNum < txBufferFrags.size()) {
            res = new NfcResponse(NfcResponse.TYPE.OK);

            /* Set the more flag is there's more than one fragment left to send */
            if(fragNum < (txBufferFrags.size() - 1)) {
                res.setMore(true);
            } else {
                txBufferAvailable = false;
            }

            /* Add the fragment as the payload */
            res.setPayload(txBufferFrags.get(fragNum));

            fragNum++;

            return res.getBytes();
        }

        /* Reader is asking for fragment after reading them all -- error */
        return new NfcResponse(NfcResponse.TYPE.ERROR).getBytes();
    }
}
