package com.example.conner.projectresearch;

public abstract class StrRunnable implements Runnable {
    protected String mStr;

    public StrRunnable(String str) {
        mStr = str;
    }
}
