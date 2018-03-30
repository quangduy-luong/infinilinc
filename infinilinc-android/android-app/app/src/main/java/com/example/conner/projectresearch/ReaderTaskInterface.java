package com.example.conner.projectresearch;

import java.util.List;

public interface ReaderTaskInterface {
    void onComplete(boolean status, List<NfcCommand> commands);
}
