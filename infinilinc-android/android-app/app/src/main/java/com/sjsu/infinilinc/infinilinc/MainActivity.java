package com.sjsu.infinilinc.infinilinc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

    private String appDomain = "infinilinc.firebaseapp.com";
    private String appPath = "/debug.html";

    private View mainLayoutView;
    private View offlineLayoutView;

    @Override
    void onNfcConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainWebView.evaluateJavascript(makeJsCallbackString("nfc.onConnect", null), null);
            }
        });
    }

    @Override
    void onNfcSendComplete(boolean success) {
        if(success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainWebView.evaluateJavascript(makeJsCallbackString("nfc.onSendComplete", null), null);
                }
            });
        }
    }

    @Override
    void onNfcReceiveComplete(boolean success, String str) {
        if(success) {
            /* Escape any quotes in the string to prevent execution of string */
            String escaped_str = str;
            escaped_str = escaped_str.replace("\'", "\\\'");
            escaped_str = escaped_str.replace("\"", "\\\"");

            runOnUiThread(new StrRunnable(escaped_str) {
                @Override
                public void run() {
                    mainWebView.evaluateJavascript(makeJsCallbackString("nfc.onReceive", mStr), null);
                }
            });
        }
    }

    void onNetStateChange(boolean connected) {
        if(connected) {
            setContentView(mainLayoutView);
            mainWebView.loadUrl("https://" + appDomain + appPath);
        } else {
            setContentView(offlineLayoutView);
            mainWebView.loadUrl("about:blank");

            disableNfc();
        }
    }

    @Override
    void onNfcReset() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainWebView.evaluateJavascript(makeJsCallbackString("nfc.onReset", null), null);
            }
        });
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
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                /* In the app, JS alerts should only be used for debugging */
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                result.confirm();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(getClass().getName(), consoleMessage.sourceId() + ":" +
                        consoleMessage.lineNumber() + " -- " + consoleMessage.message());
                return true;
            }
        });

        webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mainWebView.addJavascriptInterface(this, "nfc");

        /* Do an initial check on the network state before setting up the broadcast receiver */
        onNetStateChange(isNetConnected(this));

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

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(br, filter);
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

    private String makeJsCallbackString(String methodName, String parameter) {
        String str = "if((typeof " + methodName + " !== \'undefined\') && (" + methodName
                + " !== null)) {" + methodName + "(";

        if(parameter != null) {
            str += "\'" + parameter + "\'";
        }

        str += ");}";

        return str;
    }
}