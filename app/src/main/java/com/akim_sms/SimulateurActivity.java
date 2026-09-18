package com.akim_sms;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class SimulateurActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            webView = new WebView(this);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);

            webView.setWebViewClient(new WebViewClient());
            webView.setBackgroundColor(0xFF0A0E1A);

            webView.loadUrl("file:///android_asset/simulateur.html");

            setContentView(webView);
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText("Erreur : " + e.getMessage());
            tv.setPadding(40, 200, 40, 40);
            setContentView(tv);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
