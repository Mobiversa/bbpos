package com.mobiversa.ezy2pay.ui.notifications.notificationFcm

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.ui.splash.SplashActivity

class ChatFcmActivity : BaseActivity() {

    private lateinit var webView1: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_fcm)

        webView1 = findViewById(R.id.webView1)

        val url_test: String

        val startingIntent = intent
        if (startingIntent != null) {
            url_test = ""+startingIntent.getStringExtra("url")
            if (!url_test.equals("no_url")) {
                Log.v("--chat--", url_test)
                startWebView(url_test)
            }else{
                finish()
                startActivity(Intent(applicationContext, SplashActivity::class.java))
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(url: String) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link

        webView1.webViewClient = object : WebViewClient() {
            var progressDialog: ProgressDialog? = null

            //If you will not use this method url links are opeen in new brower not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            //Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {

            }

            override fun onPageFinished(view: WebView, url: String) {

            }
        }
        // Javascript inabled on webview
        webView1.settings.javaScriptEnabled = true

        //Load url in webview
        webView1.loadUrl(url)
    }
}
