package com.sjsu.infinilinc.infinilinc;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class InfinilincNFC {
    private static InfinilincNFC instance = null;

    private static final int NFC_READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A
            | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    private static final int TIMER_DELAY_MS = 150;

    static final int MSG_TIMER               = 0x0;
    static final int CMD_ENABLE              = 0x1;
    static final int CMD_DISABLE             = 0x2;
    static final int CMD_SEND                = 0x3;
    static final int CMD_RECEIVE             = 0x4;
    //static final int CMD_GET_ENABLED         = 0x5;
    static final int CMD_INIT                = 0x6;

    static final int EVENT_CONNECTED         = 0x7;
    static final int EVENT_DISCONNECTED      = 0x8;
    static final int EVENT_SEND_SUCCEEDED    = 0x9;
    static final int EVENT_SEND_FAILED       = 0xA;
    static final int EVENT_RECEIVE_SUCCEEDED = 0xB;
    static final int EVENT_RECEIVE_FAILED    = 0xC;

    private Semaphore initSemaphore = new Semaphore(0);
    private Lock readerModeLock = new ReentrantLock();
    private boolean initialized = false;

    private Activity parentActivity;
    private HandlerThread worker;
    private Handler uiThreadHandler;
    private Handler workerHandler;
    private LocalBroadcastManager lbm;

    private Random rng = new Random();

    private NfcAdapter adapter;
    private int mode; /* NFC mode: 0 for card em, 1 for reader */
    private boolean enabled = false;
    private boolean connected = false;

    private IsoDep tagInterface;
    private int fragmentSize;

    private boolean dataPendingFromCard = false;
    private boolean waitingForCardData = false;
    private String rxData;

    private boolean suspended = false;

    private ServiceConnection cardSrvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {}

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_TIMER:
                    if(enabled) {
                        if(mode == 1) {
                            /* Reader mode, wait for critical section */
                            readerModeLock.lock();
                            readerModeLock.unlock();
                        }

                        if(!connected) {
                            enableRandomMode();
                            startTimer();
                        }
                    }
                    break;

                case CMD_ENABLE:
                    handleEnable();
                    break;

                case CMD_DISABLE:
                    handleDisable();
                    break;

                case CMD_SEND:
                    handleSend((String)msg.obj);
                    break;

                case CMD_RECEIVE:
                    handleReceive();
                    break;

                case CMD_INIT:
                    initialized = startDriver(parentActivity);
                    initSemaphore.release();
                    break;

                default:
                    return false;
            }

            return true;
        }
    };

    private BroadcastReceiver bcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action != null) {
                switch(action) {
                    case NfcCardService.EVENT_CONNECTED:
                        connected = true;
                        notifyUiThread(EVENT_CONNECTED, null);
                        break;

                    case NfcCardService.EVENT_DISCONNECTED:
                        connected = false;
                        notifyUiThread(EVENT_DISCONNECTED, null);
                        break;

                    case NfcCardService.EVENT_RECEIVED_DATA:
                        rxData = intent.getStringExtra(Intent.EXTRA_TEXT);

                        if(waitingForCardData) {
                            waitingForCardData = false;
                            dataPendingFromCard = false;
                            notifyUiThread(EVENT_RECEIVE_SUCCEEDED, rxData);
                        } else {
                            dataPendingFromCard = true;
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    };

    private NfcAdapter.ReaderCallback readerCallback = new NfcAdapter.ReaderCallback() {
        @Override
        public void onTagDiscovered(Tag tag) {
            readerModeLock.lock();

            connected = false;

            tagInterface = IsoDep.get(tag);

            if(tagInterface != null) {
                /* Connect to the tag */
                try {
                    tagInterface.connect();
                } catch (IOException e) {
                    Log.d(getClass().getName(), "IsoDep connection to tag failed");
                    tagInterface = null;

                    readerModeLock.unlock();
                    return;
                }

                /* Select our app */
                NfcCommand selectCmd = new NfcCommand(NfcCommand.TYPE.SELECT_AID);
                if (!selectCmd.send(tagInterface)) {
                    Log.d(getClass().getName(), "IsoDep AID selection failed");
                    tagInterface = null;

                    readerModeLock.unlock();
                    return;
                }

                /* Set channel fragmentation size */
                fragmentSize = tagInterface.getMaxTransceiveLength();
                NfcCommand setDataSize = new NfcCommand(NfcCommand.TYPE.SET_DATA_SIZE);
                setDataSize.setDataSize(fragmentSize);

                if (!setDataSize.send(tagInterface)) {
                    Log.d(getClass().getName(), "NFC channel fragmentation size select failed");
                    tagInterface = null;

                    readerModeLock.unlock();
                    return;
                }

                connected = true;

                readerModeLock.unlock();

                /* Notify UI thread that we're connected */
                notifyUiThread(EVENT_CONNECTED, null);
            }
        }
    };

    private InfinilincNFC() {}

    static InfinilincNFC getInstance() {
        if(instance == null) {
            instance = new InfinilincNFC();
        }

        return instance;
    }

    boolean init(Activity activity, Handler handler) {
        if(!initialized) {
            parentActivity = activity;
            uiThreadHandler = handler;

            adapter = NfcAdapter.getDefaultAdapter(activity);

            worker = new HandlerThread("InfinilincNFC");
            worker.start();

            workerHandler = new Handler(worker.getLooper(), handlerCallback);

            workerHandler.sendEmptyMessage(CMD_INIT);

            try {
                initSemaphore.acquire();
            } catch(InterruptedException e) {
                return false;
            }

            initialized = true;
        }

        return true;
    }

    private boolean startDriver(Context context) {
        lbm = LocalBroadcastManager.getInstance(context);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcCardService.EVENT_CONNECTED);
        filter.addAction(NfcCardService.EVENT_DISCONNECTED);
        filter.addAction(NfcCardService.EVENT_RECEIVED_DATA);

        lbm.registerReceiver(bcReceiver, filter);

        Intent serviceIntent = new Intent(context, NfcCardService.class);
        if(!context.bindService(serviceIntent, cardSrvConn, Context.BIND_AUTO_CREATE)) {
            return false;
        }

        return true;
    }

    private void handleEnable() {
        /* Enable card service. If reader mode is started, it will override the card service */
        notifyCardSvc(NfcCardService.ACTION_ENABLE, null);

        enableRandomMode();
        startTimer();

        enabled = true;
    }

    private void handleDisable() {
        adapter.disableReaderMode(parentActivity);
        notifyCardSvc(NfcCardService.ACTION_DISABLE, null);

        enabled = false;
    }

    private void handleSend(String str) {
        if(!connected) {
            notifyUiThread(EVENT_SEND_FAILED, null);
            return;
        }

        if(mode == 0) {
            /* Card emulation */
            notifyCardSvc(NfcCardService.ACTION_SEND_DATA, str);
            notifyUiThread(EVENT_SEND_SUCCEEDED, null);
        } else {
            /* Reader */
            NfcCommand txCmd = new NfcCommand(NfcCommand.TYPE.SEND);
            txCmd.setDataSize(fragmentSize);
            txCmd.setPayload(str);

            if(txCmd.send(tagInterface)) {
                /* Success */
                notifyUiThread(EVENT_SEND_SUCCEEDED, null);
            } else {
                /* Fail */
                notifyUiThread(EVENT_SEND_FAILED, null);
            }
        }
    }

    private void handleReceive() {
        if(!connected) {
            notifyUiThread(EVENT_RECEIVE_FAILED, null);
            return;
        }

        if(mode == 0) {
            /* Card emulation */
            if(dataPendingFromCard) {
                dataPendingFromCard = false;
                notifyUiThread(EVENT_RECEIVE_SUCCEEDED, rxData);
            } else {
                waitingForCardData = true;
            }
        } else {
            /* Reader */
            NfcCommand rxCmd = new NfcCommand(NfcCommand.TYPE.READ);
            rxCmd.setDataSize(fragmentSize);

            if(rxCmd.send(tagInterface)) {
                /* Success */
                notifyUiThread(EVENT_RECEIVE_SUCCEEDED, rxCmd.getResponsePayloadString());
            } else {
                /* Fail */
                notifyUiThread(EVENT_RECEIVE_FAILED, null);
            }
        }
    }

    private void enableRandomMode() {
        mode = rng.nextInt() & 1;

        /* Assuming the card service is already running, toggling reader mode is sufficient to
         * switch modes */

        if(mode == 0) {
            /* Card emulation */
            adapter.disableReaderMode(parentActivity);
            Log.d("fdsa", "chose card mode");
        } else {
            /* Reader */
            adapter.enableReaderMode(parentActivity, readerCallback, NFC_READER_FLAGS, null);
            Log.d("fdsa", "chose reader mode");
        }
    }

    private void startTimer() {
        workerHandler.sendEmptyMessageDelayed(MSG_TIMER, TIMER_DELAY_MS);
    }

    private void notifyCardSvc(String action, String data) {
        Intent i = new Intent(action);
        if(data != null) {
            i.putExtra(Intent.EXTRA_TEXT, data);
        }

        lbm.sendBroadcast(i);
    }

    private void notifyUiThread(int what, Object data) {
        Message m = new Message();
        m.what = what;
        if(data != null) {
            m.obj = data;
        }

        uiThreadHandler.sendMessage(m);
    }

    private void notifyWorkerThread(int what, Object data) {
        Message m = new Message();
        m.what = what;
        if(data != null) {
            m.obj = data;
        }

        workerHandler.sendMessage(m);
    }

    void enable() {
        notifyWorkerThread(CMD_ENABLE, null);
    }

    void disable() {
        notifyWorkerThread(CMD_DISABLE, null);
    }

    void send(String str) {
        notifyWorkerThread(CMD_SEND, str);
    }

    void receive() {
        notifyWorkerThread(CMD_RECEIVE, null);
    }

    void suspend() {
        if(!suspended) {
            /* Unbind from the card service and disable reader mode */
            parentActivity.unbindService(cardSrvConn);
            adapter.disableReaderMode(parentActivity);

            suspended = true;
        }
    }

    void resume() {
        if(suspended) {
            /* Rebind the card service */
            Intent serviceIntent = new Intent(parentActivity, NfcCardService.class);
            parentActivity.bindService(serviceIntent, cardSrvConn, Context.BIND_AUTO_CREATE);

            suspended = false;
        }
    }
}
