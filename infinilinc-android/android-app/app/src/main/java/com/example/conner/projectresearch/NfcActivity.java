package com.example.conner.projectresearch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class defining NFC activites for main Activity.
 */
public abstract class NfcActivity extends Activity implements ReaderTaskInterface {
    protected enum NfcMode {READER, CARD_EM};

    private NfcMode nfcMode = NfcMode.CARD_EM;
    private boolean nfcEnabled = false;
    private NfcAdapter nfcAdapter;

    private IsoDep tagInterface = null;

    /* Service connection variables */
    private Intent serviceIntent;
    private Messenger serviceMessenger;
    private IntentFilter serviceIntentFilter;
    private BroadcastReceiver broadcastReceiver;
    private boolean serviceIsBound = false;

    private ServiceConnection serviceConn;

    private String receivedString = null;
    private boolean newReceivedData = false;
    private boolean waitingForReceivedData = false;

    private int fragmentSize = 0;

    /* Implemented by UI class */
    abstract void onConnect();
    abstract void onDisconnect();
    abstract void onSendComplete(boolean success);
    abstract void onReceiveComplete(boolean success, String str);

    @JavascriptInterface
    public final boolean exists() {
        return true;
    }

    @JavascriptInterface
    public final void enable() {
        enableNfc();
    }

    @JavascriptInterface
    public final void disable() {
        disableNfc();
    }

    @JavascriptInterface
    public final void send(String str) {
        if(!nfcEnabled) {
            return;
        }

        if(nfcMode == NfcMode.READER) {
            /* Start a transmit exchange with the card */
            NfcCommand txCmd = new NfcCommand(NfcCommand.TYPE.SEND);
            txCmd.setDataSize(fragmentSize);
            txCmd.setPayload(str);

            List<NfcCommand> cmdList = new ArrayList<>();
            cmdList.add(txCmd);

            ReaderCmdTask sendTask = new ReaderCmdTask(tagInterface, cmdList, this);
            sendTask.execute();
        } else {
            /* Send the string to the card emulation service */
            LocalBroadcastManager l = LocalBroadcastManager.getInstance(getApplicationContext());

            Intent intent = new Intent(NfcCardService.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, str);

            l.sendBroadcast(intent);

            onSendComplete(true);
        }
    }

    @JavascriptInterface
    public final void receive() {
        if(!nfcEnabled) {
            return;
        }

        if(nfcMode == NfcMode.READER) {
            NfcCommand rxCmd = new NfcCommand(NfcCommand.TYPE.READ);
            rxCmd.setDataSize(fragmentSize);

            List<NfcCommand> cmdList = new ArrayList<>();
            cmdList.add(rxCmd);

            ReaderCmdTask receiveTask = new ReaderCmdTask(tagInterface, cmdList, this);
            receiveTask.execute();
        } else {
            if(newReceivedData) {
                newReceivedData = false;
                onReceiveComplete(true, receivedString);
            } else {
                waitingForReceivedData = true;
            }
        }
    }

    @JavascriptInterface
    public final void disconnect() {
        disconnectFromTag();
    }

    @JavascriptInterface
    public final void setMode(int mode) {
        /* 0 for card emulation, 1 for reader */
        setNfcMode((mode == 0) ? NfcMode.CARD_EM: NfcMode.READER);
    }

    @JavascriptInterface
    public final int mode() {
        return (nfcMode == NfcMode.CARD_EM) ? 0 : 1;
    }

    @JavascriptInterface
    public final boolean enabled() {
        return nfcEnabled;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Context c = getApplicationContext();
        serviceIntent = new Intent(c, NfcCardService.class);

        /* Add actions to the service intent filter */
        serviceIntentFilter = new IntentFilter();
        serviceIntentFilter.addAction(NfcCardService.ACTION_CONNECT);
        serviceIntentFilter.addAction(NfcCardService.ACTION_DISCONNECT);
        serviceIntentFilter.addAction(NfcCardService.ACTION_RECEIVED);

        /* Create the broadcast receiver and give it a handler method */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action != null) {
                    switch(action) {
                        case NfcCardService.ACTION_CONNECT:
                            onConnect();
                            break;

                        case NfcCardService.ACTION_DISCONNECT:
                            onDisconnect();
                            break;

                        case NfcCardService.ACTION_RECEIVED:
                            receivedString = intent.getStringExtra(Intent.EXTRA_TEXT);

                            if(waitingForReceivedData) {
                                waitingForReceivedData = false;
                                newReceivedData = false;
                                onReceiveComplete(true, receivedString);
                            } else {
                                newReceivedData = true;
                            }

                            break;
                    }
                }
            }
        };

        /* Give the service connection handler methods */
        serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceMessenger = new Messenger(iBinder);

                LocalBroadcastManager l = LocalBroadcastManager.getInstance(NfcActivity.this);
                l.registerReceiver(broadcastReceiver, serviceIntentFilter);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    protected void disableReader() {
        nfcAdapter.disableReaderMode(this);
    }

    protected void enableReader() {
        nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {
                tagInterface = IsoDep.get(tag);

                if(tagInterface != null) {
                    /* Connect to the tag */
                    try {
                        tagInterface.connect();
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep connection to tag failed");
                        return;
                    }

                    /* Select our app */
                    NfcCommand selectCmd = new NfcCommand(NfcCommand.TYPE.SELECT_AID);
                    if(!selectCmd.send(tagInterface)) {
                        Log.d(getClass().getName(), "IsoDep AID selection failed");
                        return;
                    }

                    /* Set channel fragmentation size */
                    fragmentSize = tagInterface.getMaxTransceiveLength();
                    NfcCommand setDataSize = new NfcCommand(NfcCommand.TYPE.SET_DATA_SIZE);
                    setDataSize.setDataSize(fragmentSize);

                    if(!setDataSize.send(tagInterface)) {
                        Log.d(getClass().getName(), "NFC channel fragmentation size select failed");
                        return;
                    }

                    /* Notify UI thread that we're connected */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onConnect();
                        }
                    });
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);

        nfcMode = NfcMode.READER;
    }

    protected void disableCardEm() {
        /* Disconnect from the service */
        if(serviceIsBound) {
            unbindService(serviceConn);

            serviceIsBound = false;
        }
    }

    protected void enableCardEm() {
        bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);

        serviceIsBound = true;
        nfcMode = NfcMode.CARD_EM;
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableNfc();
    }

    protected void setNfcMode(NfcMode m) {
        if(nfcEnabled) {
            switch(m) {
                case READER:
                    disableCardEm();
                    enableReader();
                    break;

                case CARD_EM:
                    disableReader();
                    enableCardEm();
                    break;

                default:
                    throw new RuntimeException("Unreachable");
            }
        }

        nfcMode = m;
    }

    protected void enableNfc() {
        switch(nfcMode) {
            case READER:
                enableReader();
                break;

            case CARD_EM:
                enableCardEm();
                break;
        }

        nfcEnabled = true;
    }

    protected void disableNfc() {
        disconnectFromTag();
        disableReader();
        disableCardEm();

        nfcEnabled = false;
    }

    @Override
    public void onComplete(boolean status, List<NfcCommand> commands) {
        switch(commands.get(0).getType()) {
            case SEND:
                onSendComplete(status);
                break;

            case READ:
                onReceiveComplete(status, commands.get(0).getResponsePayloadString());
                break;
        }
    }

    protected void disconnectFromTag() {
        if(tagInterface != null) {
            try {
                tagInterface.close();
            } catch(IOException e) {
                Log.d(getClass().getName(), "Tag IsoDeop interface threw IOException");
            }

            tagInterface = null;
        }
    }
}
