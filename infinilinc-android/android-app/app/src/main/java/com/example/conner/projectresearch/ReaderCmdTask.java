package com.example.conner.projectresearch;

import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class ReaderCmdTask extends AsyncTask<Void, Void, Boolean> {
    private ReaderTaskInterface mCallbackInterface;
    private IsoDep mTag;
    private List<NfcCommand> mCommands;

    public ReaderCmdTask(IsoDep tag, List<NfcCommand> commands,
                         ReaderTaskInterface callbackInterface) {
        mCallbackInterface = callbackInterface;
        mTag = tag;
        mCommands = commands;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        /* Send the listed commands */
        for(int i = 0; i < mCommands.size(); i++) {
            if(!mCommands.get(i).send(mTag)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        mCallbackInterface.onComplete(success, mCommands);
    }
}
