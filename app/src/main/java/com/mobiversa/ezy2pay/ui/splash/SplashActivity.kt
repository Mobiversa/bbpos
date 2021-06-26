package com.mobiversa.ezy2pay.ui.splash

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.RootUtil
import java.text.SimpleDateFormat
import java.util.*


class SplashActivity : BaseActivity() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val prefs_data: SharedPreferences = PreferenceHelper.customPrefs(this, "RegData")
        val editor = prefs_data.edit().clear()
        editor.apply()

        FirebaseApp.initializeApp(applicationContext);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        startAnimations() //start the animation
        val mHandler = Handler()
        val mRunnable = Runnable {
            //Check if the device is Rooted or not - if rooted exit the app (For PCI)
            if (RootUtil.isDeviceRooted) {
                showAlertDialogAndExitApp(Constants.ROOTED_DEVICE)
            } else {
                val pref = applicationContext.getSharedPreferences(
                    "EzyWire",
                    Context.MODE_PRIVATE
                )
                val today = Calendar.getInstance().time
                var login = true
                try {
                    val lastLoginStr = pref.getString("LastLoggedIn", "")
                    val curFormater =
                        SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                    if (lastLoginStr!!.isNotEmpty()) {
                        val lastLogIn = curFormater.parse(lastLoginStr)
                        val timeDifference = today.time - lastLogIn.time
                        if (timeDifference / (60 * 60 * 1000) < 24) {
                            login = false
                        }
                    } else {
                        val editor = pref.edit()
                        editor.remove("MerchantDetail")
                        editor.remove("LastLoggedIn")
                        editor.apply()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.v("--callFromSplash--", "intent")
                //Intent intent = new Intent(getApplicationContext(), login ? EzywireLogin.class : EzywireHome.class).putExtra("fromlogin", "Yes");
                val intent = Intent(
                    applicationContext,  /*login ? EzywireLogin.class : */
                    LoginActivity::class.java
                )
                startActivity(intent)
                finish()
                //Intent intent = new Intent(getApplicationContext(), login ? EzywireLogin.class : NavMainActivity.class).putExtra("fromlogin", "Yes");
            }
        }
        mHandler.postDelayed(mRunnable, 5000)
    }


    /*Splash Text animation*/

    /*Splash Text animation*/
    private fun startAnimations() {
        var anim =
            AnimationUtils.loadAnimation(this, R.anim.alpha)
        anim.reset()
        val l = findViewById<View>(R.id.linearLayout) as LinearLayout
        l.clearAnimation()
        l.startAnimation(anim)
        anim = AnimationUtils.loadAnimation(this, R.anim.translate)
        anim.reset()
        val iv =
            findViewById<View>(R.id.imageView4) as AppCompatImageView
        val tv = findViewById<View>(R.id.textView) as AppCompatTextView
        iv.clearAnimation()
        tv.clearAnimation()
        iv.startAnimation(anim)
        tv.startAnimation(anim)
    }


    /*Close the app and exit*/

    /*Close the app and exit*/
    fun showAlertDialogAndExitApp(message: String?) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }
}
