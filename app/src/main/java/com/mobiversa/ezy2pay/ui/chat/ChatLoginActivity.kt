package com.mobiversa.ezy2pay.ui.chat

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Encryptor
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.activity_chat_login.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLoginActivity : AppCompatActivity() {

    private lateinit var hex_to_asci_name: String
    lateinit var fcm_id: String
    private lateinit var prefs: SharedPreferences

    lateinit var date: Date
    lateinit var formatter: SimpleDateFormat
    lateinit var today: String
    lateinit var date_today: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_login)

        supportActionBar?.title = "MobiChat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefs = PreferenceHelper.defaultPrefs(this)


        fcm_id = getToken()
        date = Calendar.getInstance().time

        // Display a date in day, month, year format
        formatter = SimpleDateFormat("ddMMyyyy")
        today = formatter.format(date)

        date_today = today + today

        getChatURL()
    }

    private fun getChatURL(){
        val name: String? = intent.getStringExtra("name")
        val phone: String? = intent.getStringExtra("phone")
        val company: String? = intent.getStringExtra("company")

        if (company != "") {

            Log.v("--company--", "beforeLogin")
            println("--Today : $today")

            date_today = today + today

            var to_encrypt: String = "$name#$phone#$company"

            val value_encrypted = Encryptor.encrypt(date_today, date_today, to_encrypt)
            hex_to_asci_name = Encryptor.encodeHexString(value_encrypted!!)
            Log.v("--hex--", hex_to_asci_name)

            /*var to_encry_fcm = fcm_id
            val fcm_encrypted = Encryptor.encrypt(date_today, date_today, to_encry_fcm)
            Log.v("--fcm_encrypted--", fcm_encrypted)
            hex_to_asci_fcm = Encryptor.encodeHexString(fcm_encrypted)
            Log.v("--hex--", hex_to_asci_fcm)*/

//            var fcm_data: String = encrypt_fcm(fcm_id)

            startWebView(Constants.CHAT_URL
                    + hex_to_asci_name + "&dt=" + date_today + "&d=a" + "&token=" + fcm_id)

        } else {

            Log.v("--nocompany--", "beforeLogin")
            println("--Today : $today")

            date_today = today + today

            var to_encrypt: String = "$name#$phone"

            val value_encrypted = Encryptor.encrypt(date_today, date_today, to_encrypt)
            hex_to_asci_name = Encryptor.encodeHexString(value_encrypted!!)
            Log.v("--hex--", hex_to_asci_name)

            startWebView(Constants.CHAT_URL
                    + hex_to_asci_name + "&dt=" + date_today + "&d=a" + "&token=" + fcm_id)

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(url: String) {

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
//                cancelDialog()
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
}
