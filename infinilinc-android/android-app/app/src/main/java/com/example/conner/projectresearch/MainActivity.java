package com.example.conner.projectresearch;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends NfcActivity {
    private WebView mainWebView;
    private WebSettings webSettings;

    private String appDomain = "192.168.1.70";

    @Override
    void onConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainWebView.evaluateJavascript("nfc.onConnect();", null);
            }
        });
    }

    @Override
    void onDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainWebView.evaluateJavascript("nfc.onDisconnect();", null);
            }
        });
    }

    @Override
    void onSendComplete(boolean success) {
        if(success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainWebView.evaluateJavascript("nfc.onSendComplete();", null);
                }
            });
        }
    }

    @Override
    void onReceiveComplete(boolean success, String str) {
        if(success) {
            /* Escape any quotes in the string to prevent execution of string */
            String escaped_str = str;
            escaped_str = escaped_str.replace("\'", "\\\'");
            escaped_str = escaped_str.replace("\"", "\\\"");

            runOnUiThread(new StrRunnable(escaped_str) {
                @Override
                public void run() {
                    mainWebView.evaluateJavascript("nfc.onReceive(\'" + mStr + "\');", null);
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainWebView = (WebView)findViewById(R.id.main_web_view);

        mainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(Uri.parse(url).getHost().equals(appDomain)) {
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);

                return true;
            }
        });

        mainWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                /* In the app, JS alerts should only be used for debugging */
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                result.confirm();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(getClass().getName(), consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + " -- " + consoleMessage.message());
                return true;
            }
        });

        webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mainWebView.addJavascriptInterface(this, "nfc");

        mainWebView.loadUrl("http://" + appDomain + "/");
    }

    @Override
    public void onBackPressed() {
        if(mainWebView.canGoBack()) {
            mainWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}