package com.sjsu.infinilinc.infinilinc;

abstract class StrRunnable implements Runnable {
    String mStr;

    StrRunnable(String str) {
        mStr = str;
    }
}
