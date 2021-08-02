package com.mobiversa.ezy2pay

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.utils.*
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.math.min


class MainActivity : BaseActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if(resources.getBoolean(R.bool.portrait_only)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_history, R.id.navigation_notifications, R.id.navigation_settings ))
        /*val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_notifications,
                R.id.navigation_chat, R.id.navigation_settings
            )
        )*/
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        //Network Connection Check
        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
//        LocationService.init(this)

        val current = Calendar.getInstance().time

        val sdfComp = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val currentLogin = sdfComp.format(current)
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(applicationContext)

        val lastLogin = prefs.getString(Constants.LastLogin,"")
        if (lastLogin.equals("")){
            prefs[Constants.LastLogin] = currentLogin
            if (getLoginResponse(applicationContext).hostType.equals("U",true) && Constants.EzyMoto.equals("EZYLINK",true) && getProductList()[0].isEnable)
            showReferralPrompt()
        }else{
            val date = DateFormatter.DateComparison(currentLogin, lastLogin)
            when(date){
                "before"->{
                    if (getLoginResponse(applicationContext).hostType.equals("U",true) && getProductList()[0].isEnable)
                        showReferralPrompt()
                }
            }

        }
        prefs[Constants.LastLogin] = currentLogin

        navView.itemIconTintList = null

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar = Snackbar.make(
                findViewById(R.id.container),
                "You are offline",
                Snackbar.LENGTH_LONG
            ) //Assume "rootLayout" as the root layout of every activity.
            snackBar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackBar?.show()

            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            snackBar?.dismiss()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onBackPressed() {
        showExitAlert("Exit Alert","Are you sure you want to exit from Mobi?")
    }

    private fun showUpdatePrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = this.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_update, null)
        val positiveBtn = alertLayout.findViewById<View>(R.id.positive_btn) as Button
        val negativeBtn = alertLayout.findViewById<View>(R.id.negative_btn) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        positiveBtn.setOnClickListener {
            val appPackageName = "com.mobiversa.ezy2pay"// getPackageName() from Context or Activity object

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName"))
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        //startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    private fun showReferralPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = this.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_referal_main, null)
        val positiveBtn = alertLayout.findViewById<View>(R.id.referal_txt) as TextView
        val negativeBtn = alertLayout.findViewById<View>(R.id.alert_close_button) as ImageView
        val mBuilder = this.let {
            AlertDialog.Builder(it)
                .setView(alertLayout)
        }

        positiveBtn.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://gomobi.io/referral/?mid=${getLoginResponse(applicationContext).motoMid}")
            startActivity(openURL)
            mAlertDialog.dismiss()
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(this.resources.getColor(android.R.color.transparent)))
        val dialogWindow = mAlertDialog.window
        dialogWindow?.setGravity(Gravity.CENTER)
    }
}

