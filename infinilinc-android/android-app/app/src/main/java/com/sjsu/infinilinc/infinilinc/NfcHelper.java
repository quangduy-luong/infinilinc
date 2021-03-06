package com.sjsu.infinilinc.infinilinc;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class NfcHelper {
    static List<byte[]> fragment(byte[] bytes, int fragmentSize) {
        if(fragmentSize < 0) {
            Log.d("NfcHelper.fragment", "a");
            throw new RuntimeException();
        }
        else if(((bytes.length > 0) && (fragmentSize == 0))) {
            Log.d("NfcHelper.fragment", "b");
            throw new RuntimeException();
        }

        List<byte[]> frags = new ArrayList<>();

        byte[] f;
        int i = 0;

        while((bytes.length - i) > fragmentSize) {
            f = new byte[fragmentSize];
            System.arraycopy(bytes, i, f, 0, fragmentSize);

            frags.add(f);

            i += fragmentSize;
        }

        f = new byte[bytes.length - i];
        System.arraycopy(bytes, i, f, 0, bytes.length - i);

        frags.add(f);

        return frags;
    }
}
