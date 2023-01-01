package com.yuvix25.kahootanswerbot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    WebView kahootWebView;
    final String[] cssInjections = {"style.css"};
    final String[] jsInjections = {"explorer.js", "content.js"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setContext(this);

        kahootWebView = findViewById(R.id.webview);

        WebSettings settings = kahootWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setDomStorageEnabled(true);
        kahootWebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        kahootWebView.setWebChromeClient(new WebChromeClient());
        kahootWebView.addJavascriptInterface(new MyJavascriptInterface(this, kahootWebView), "KahootAnswerBot");

        kahootWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    for (String file : cssInjections) {
                        injectCSS(file);
                    }
                    for (String file : jsInjections) {
                        injectJs(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                super.onPageFinished(view, url);
            }
        });

        kahootWebView.loadUrl("http://kahoot.it");
    }

    protected void injectJs(String file, boolean isContent) {
        kahootWebView.evaluateJavascript("(() => { try{return JSIsInjected;} catch {return \"null\";} })()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                if (s.equals("true")) return;

                String js = isContent ? file : Utils.readAsset(file);
                kahootWebView.evaluateJavascript(js, null);
            }
        });
    }

    protected void injectJs(String filename) {
        injectJs("js/" + filename, false);
    }



    protected void injectCSS(String filename) throws IOException {
        String css = Utils.readAsset("css/" + filename);
        injectJs("(function() {" +
            "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = `" + css + "`;" +
                    "parent.appendChild(style)" +
                    "})()", true);
    }
}