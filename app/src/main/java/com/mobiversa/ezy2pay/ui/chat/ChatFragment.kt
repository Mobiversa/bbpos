package com.mobiversa.ezy2pay.ui.chat

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mobiversa.ezy2pay.MainActivity

import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Encryptor
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.chat_fragment.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : BaseFragment() {

    var tid : String = ""
    var mid : String = ""

    private lateinit var fcmId: String
    private lateinit var prefs: SharedPreferences

    companion object {
        fun newInstance() = ChatFragment()
    }

    lateinit var date: Date
    lateinit var formatter: SimpleDateFormat
    lateinit var today: String
    private lateinit var dateToday: String

    private lateinit var hex_to_asci_name: String
    lateinit var chat_web_view: WebView

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.chat_fragment, container, false)
        setTitle("MobiChat", true)
//        (activity as MainActivity).supportActionBar?.hide()

        chat_web_view = rootView.chat_web_view

        prefs = PreferenceHelper.defaultPrefs(this.context!!)

        when {
            getLoginResponse().tid.isNotEmpty() -> {
                tid = getLoginResponse().tid
                mid = getLoginResponse().mid.toString()
            }
            getLoginResponse().motoTid.isNotEmpty() -> {
                tid = getLoginResponse().motoTid
                mid = getLoginResponse().motoMid
            }
            getLoginResponse().ezypassTid.isNotEmpty() -> {
               tid = getLoginResponse().ezypassTid
               mid = getLoginResponse().ezypassMid
            }
            getLoginResponse().ezyrecTid.isNotEmpty() -> {
               tid = getLoginResponse().ezyrecTid
               mid = getLoginResponse().ezyrecMid
            }
        }

        fcmId = getToken()
        date = Calendar.getInstance().time

        // Display a date in day, month, year format
        formatter = SimpleDateFormat("ddMMyyyy")
        today = formatter.format(date)

        dateToday = today + today

        var to_encrypt: String = getLoginResponse().merchantName + "#" + mid + "#" + tid

        val value_encrypted = Encryptor.encrypt(dateToday, dateToday, to_encrypt)
        Log.v("--encrypt--", ""+value_encrypted)
        hex_to_asci_name = Encryptor.encodeHexString(value_encrypted!!)
        Log.v("--hex--", hex_to_asci_name)

        startWebView(Constants.LOGGED_CHAT_URL
                + hex_to_asci_name + "&dt=" + dateToday + "&d=a" + "&token=" + fcmId)

        return rootView
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

}
