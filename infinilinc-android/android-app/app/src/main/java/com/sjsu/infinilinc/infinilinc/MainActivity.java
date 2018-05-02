package com.sjsu.infinilinc.infinilinc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView mainWebView;
    private WebSettings webSettings;

    private String appDomain = "infinilinc.firebaseapp.com";
    private String appPath = "/";

    private View mainLayoutView;
    private View offlineLayoutView;

    private InfinilincNFC nfcDriver = InfinilincNFC.getInstance();

    private boolean nfcReset = false;

    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String js, param;

            switch(msg.what) {
                case InfinilincNFC.EVENT_CONNECTED:
                    js = makeJsCallbackString("nfc.onConnect", null);
                    mainWebView.evaluateJavascript(js, null);
                    break;

                case InfinilincNFC.EVENT_DISCONNECTED:
                    break;

                case InfinilincNFC.EVENT_SEND_SUCCEEDED:
                    js = makeJsCallbackString("nfc.onSendComplete", null);
                    mainWebView.evaluateJavascript(js, null);
                    break;

                case InfinilincNFC.EVENT_SEND_FAILED:
                    break;

                case InfinilincNFC.EVENT_RECEIVE_SUCCEEDED:
                    param = (String)msg.obj;

                    /* Escape any quotes in the string to prevent execution of string */
                    param = param.replace("\'", "\\\'");
                    param = param.replace("\"", "\\\"");

                    js = makeJsCallbackString("nfc.onReceive", param);
                    mainWebView.evaluateJavascript(js, null);
                    break;

                case InfinilincNFC.EVENT_RECEIVE_FAILED:
                    break;

                default:
                    return false;
            }

            return true;
        }
    };

    /* Broadcast receiver to handle changes in network state */
    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onNetStateChange(isNetConnected(context));
                    }
                });
            }
        }
    };

    void onNetStateChange(boolean connected) {
        if(connected) {
            setContentView(mainLayoutView);
            mainWebView.loadUrl("https://" + appDomain + appPath);
        } else {
            setContentView(offlineLayoutView);
            mainWebView.loadUrl("about:blank");

            nfcDriver.disable();
        }
    }

    boolean isNetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return ((netInfo != null) && (netInfo.isConnectedOrConnecting()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Start NFC driver */
        nfcDriver.init(this, new Handler(handlerCallback));

        LayoutInflater inflater = getLayoutInflater();

        mainLayoutView = inflater.inflate(R.layout.activity_main, null);
        offlineLayoutView = inflater.inflate(R.layout.offline, null);

        mainWebView = mainLayoutView.findViewById(R.id.main_web_view);

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
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("JavaScript console", consoleMessage.sourceId() + ":" +
                        consoleMessage.lineNumber() + " -- " + consoleMessage.message());
                return true;
            }
        });

        webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mainWebView.addJavascriptInterface(this, "nfc");

        /* Do an initial check on the network state before setting up the broadcast receiver */
        onNetStateChange(isNetConnected(this));
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
    protected void onPause() {
        super.onPause();

        unregisterReceiver(br);

        nfcDriver.suspend();
        nfcReset = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(br, filter);

        if(nfcReset) {
            nfcReset = false;

            nfcDriver.resume();

            String js = makeJsCallbackString("nfc.onReset", null);
            mainWebView.evaluateJavascript(js, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        nfcDriver.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private String makeJsCallbackString(String methodName, String parameter) {
        String str = "if((typeof " + methodName + " !== \'undefined\') && (" + methodName
                + " !== null)) {" + methodName + "(";

        if(parameter != null) {
            str += "\'" + parameter + "\'";
        }

        str += ");}";

        return str;
    }

    /* JavaScript API methods */

    /**
     * Trivial function to allow detection of NFC JS interface
     *
     * @return Always returns true
     */
    @JavascriptInterface
    public final boolean exists() {
        return true;
    }

    /**
     * Enables the NFC interface in the configured operating mode
     */
    @JavascriptInterface
    public final void enable() {
        nfcDriver.enable();
    }

    /**
     * Disables the NFC interface
     */
    @JavascriptInterface
    public final void disable() {
        nfcDriver.disable();
    }

    /**
     * Sends a string to the NFC interface
     *
     * @param str String to be sent
     */
    @JavascriptInterface
    public final void send(String str) {
        nfcDriver.send(str);
    }

    /**
     * Receives a string from the connected NFC device
     */
    @JavascriptInterface
    public final void receive() {
        nfcDriver.receive();
    }
}