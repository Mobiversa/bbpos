package com.mobiversa.ezy2pay.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Encryptor
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import java.text.SimpleDateFormat
import java.util.*

class ChatNotifyActivity : BaseActivity() {

    var tid: String = ""
    var mid: String = ""

    lateinit var fcm_id: String
    private lateinit var prefs: SharedPreferences


    lateinit var date: Date
    lateinit var formatter: SimpleDateFormat
    lateinit var today: String
    lateinit var date_today: String

    lateinit var hex_to_asci_name: String
    lateinit var chat_web_view: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_fragment)

        prefs = PreferenceHelper.defaultPrefs(this)

        supportActionBar?.title = "MobiChat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when {
            getLoginResponse(applicationContext).tid.isNotEmpty() -> {
                tid = getLoginResponse(applicationContext).tid
                mid = getLoginResponse(applicationContext).mid.toString()
            }
            getLoginResponse(applicationContext).motoTid.isNotEmpty() -> {
                tid = getLoginResponse(applicationContext).motoTid
                mid = getLoginResponse(applicationContext).motoMid
            }
            getLoginResponse(applicationContext).ezypassTid.isNotEmpty() -> {
                tid = getLoginResponse(applicationContext).ezypassTid
                mid = getLoginResponse(applicationContext).ezypassMid
            }
            getLoginResponse(applicationContext).ezyrecTid.isNotEmpty() -> {
                tid = getLoginResponse(applicationContext).ezyrecTid
                mid = getLoginResponse(applicationContext).ezyrecMid
            }
        }

        fcm_id = getToken()
        date = Calendar.getInstance().time

        // Display a date in day, month, year format
        formatter = SimpleDateFormat("ddMMyyyy")
        today = formatter.format(date)

        date_today = today + today

        var to_encrypt: String = getLoginResponse(applicationContext).merchantName + "#" + mid + "#" + tid

        val value_encrypted = Encryptor.encrypt(date_today, date_today, to_encrypt)
        Log.v("--encrypt--", ""+value_encrypted)
        hex_to_asci_name = Encryptor.encodeHexString(value_encrypted!!)
        Log.v("--hex--", hex_to_asci_name)

        startWebView(
            Constants.LOGGED_CHAT_URL
                    + hex_to_asci_name + "&dt=" + date_today + "&d=a" + "&token=" + fcm_id
        )

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(url: String) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link
        showDialog("Loading")

        chat_web_view.webViewClient = object : WebViewClient() {

            //If you will not use this method url links are opeen in new brower not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            //Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {
//                showDialog("Loading")
            }

            override fun onPageFinished(view: WebView, url: String) {
                cancelDialog()
            }
        }
        // Javascript inabled on webview
        chat_web_view.settings.javaScriptEnabled = true

        //Load url in webview
        chat_web_view.loadUrl(url)
    }

    private fun getToken(): String {
        val fcmToken: String? = prefs[Constants.FIRE_BASE_TOKEN]
        return fcmToken.toString()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
