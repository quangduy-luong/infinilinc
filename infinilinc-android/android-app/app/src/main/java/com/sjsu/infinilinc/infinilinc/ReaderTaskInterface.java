package com.sjsu.infinilinc.infinilinc;

import java.util.List;

public interface ReaderTaskInterface {
    void onComplete(boolean status, List<NfcCommand> commands);
}
